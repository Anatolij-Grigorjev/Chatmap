import com.relayrides.pushy.apns.ApnsEnvironment
import com.relayrides.pushy.apns.ApnsPushNotification
import com.relayrides.pushy.apns.PushManager
import com.relayrides.pushy.apns.PushManagerConfiguration
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder
import com.relayrides.pushy.apns.util.SSLContextUtil
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification
import com.relayrides.pushy.apns.util.TokenUtil

import javax.net.ssl.SSLHandshakeException

class BootStrap {

    def grailsApplication
    def grails

    PushManager pushManagerDev
    PushManager pushManagerProd

    def init = { servletContext ->

        grails = grailsApplication.config.grails
        pushManagerDev = buildPushy(grails, 'dev')
        pushManagerProd = buildPushy(grails, 'prod')
        pushManagerDev?.metaClass?.otherManager = pushManagerProd
        registerListeners(pushManagerDev)
        registerListeners(pushManagerProd)
        pushManagerProd?.start()
        log.debug("Push manager ${pushManagerProd?.name} initialized!")
        pushManagerDev?.start()
        log.debug("Push manager ${pushManagerDev?.name} initialized!")

    }

    private PushManager buildPushy(ConfigObject grails, String env) {
        def managerConfig = new PushManagerConfiguration()
        def apnsEnv = ApnsEnvironment."${grails.apns."${env}".environment}Environment"
        try {
            def sslCtx = SSLContextUtil.createDefaultSSLContext((String) grails.apns."${env}".p12.path,
                    (String) grails.apns."${env}".p12.password)
            def pushManager = new PushManager<ApnsPushNotification>(
                    apnsEnv,
                    sslCtx,
                    null, //event loop group
                    null, //executor service
                    null, //blocking queue
                    managerConfig,
                    (String) grails.apns."${env}".manager.name);
            return pushManager
        } catch (Exception e) {
            log.debug('Problem with APNS config: ' + e.message)
            e.printStackTrace()
        }
    }

    def addApnsMethods(def klass) {
        klass.metaClass.static.apnsManager = pushManagerDev;
        klass.metaClass.static.viaApns = { Closure actions ->
            log.debug('Invoking APNS manager ' + pushManagerDev.name)
            actions(pushManagerDev);
            log.debug('Done with via APNS')
        }

        klass.metaClass.static.sendNotification = { token, builder ->
            log.debug("Transalting token ${token} to bytes")
            def tokenBytes = TokenUtil.tokenStringToByteArray(token)

            ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder()
            log.debug('Applying payload builder...')
            builder(payloadBuilder)

            def payload = payloadBuilder.buildWithDefaultMaximumLength()

            viaApns { apns ->
                def q = apns.getQueue()
                log.debug("Got Q: ${q}")
                q.put(new SimpleApnsPushNotification(tokenBytes, payload))
                log.debug('Message put in q!')
            }

            log.debug('Done sending notification')
        }

        //classes with device token saved in them can curry the apns closure
        if (klass.metaClass.hasProperty('deviceToken')) {
            klass.metaClass.registerDeviceToken = { token ->
                klass.metaClass.pushNotification = sendNotification.curry(token)
            }
        }
    }


    def registerListeners = { PushManager theManager ->

        theManager?.registerRejectedNotificationListener { manager, notification, reason ->
            log.error "Notification ${notification} from manager ${manager.name} was rejected for reason ${reason}."
            if (manager.hasProperty('otherManager')) {
                PushManager prodPushy = manager.otherManager
                if (prodPushy) {
                    log.info("Trying the other manager, ${prodPushy.name}!")
                    def q = prodPushy.getQueue()
                    log.debug("Got q: ${q}")
                    if (q != null) {
                        q.put(notification)
                        log.debug('Message put in q! Done with 2nd manager!')
                    }
                }
            }
        }

        theManager?.registerFailedConnectionListener { manager, cause ->

            if (cause instanceof SSLHandshakeException) {
                //need to shutdown manager since no more SSL
                log.fatal "SSL Certificate expired/invalid (${cause.message})! Shutting down push service..."
                pushManagerDev.shutDown()
            }

        }

    }


    def destroy = {
        if (pushManagerDev?.isStarted()) {
            pushManagerDev.shutDown()
            //sleep after shutting down PUSHY to not leave Netty memory leaks
            //see https://github.com/relayrides/pushy/issues/29
            Thread.sleep(2500)
        }
    }
}
