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

        "/chat/$id1/$id2" {
            constraints {
                id1 matches: /\d+/
                id2 matches: /\d+/
            }
            controller = 'chat'
            action = 'index'
        }

        "/map/$id" {
            constraints {
                id matches: /\d+/
            }
            controller = 'map'
            action = 'index'
        }

        "/users/$id" {
            constraints {
                id matches: /\d+/
            }
            controller = 'users'
            action = 'index'
        }

        "/chat/send/$id" {
            constraints {
                id matches: /\d+/
            }
            controller = 'chat'
            action = 'send'
        }

        "/"(view: "/index")
        "500"(view: '/error')
    }

}
