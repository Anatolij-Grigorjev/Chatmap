package lt.mediapark.chatmap

import grails.converters.JSON
import org.springframework.web.multipart.commons.CommonsMultipartFile

class PictureController {

    static allowedMethods = [
            index : 'GET',
            upload: 'POST'
    ]

    def index = {
        def picture = Picture.get(Long.parseLong(params.id))
        if (picture) {
            response.contentType = 'image/jpeg'
            response.setHeader('Content-disposition', "attachment;filename=${picture.id}.jpg")
            response.contentLength = picture.data.length

            response.outputStream << new ByteArrayInputStream(picture.data)
            response.outputStream.flush()

            return null
        } else {
            render(status: 404)
        }
    }

    def upload = {

        CommonsMultipartFile picture = request.getFile('picture')

        def (lat, lng) = [Double.parseDouble(params.lat), Double.parseDouble(params.lng)]
        Picture pic = new Picture(name: picture.name, data: picture.bytes, lat: lat, lng: lng)
        pic = pic.save()

        render([picId: pic.id]) as JSON

    }
}
