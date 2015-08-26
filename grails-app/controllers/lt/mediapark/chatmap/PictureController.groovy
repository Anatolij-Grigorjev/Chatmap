package lt.mediapark.chatmap

class PictureController {

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
}
