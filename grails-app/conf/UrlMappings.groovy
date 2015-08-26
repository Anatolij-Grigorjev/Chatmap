class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        "/picture/upload/$lat/$lng" {
            controller = 'picture'
            action = 'upload'
        }

        "/"(view: "/index")
        "500"(view: '/error')
    }

}
