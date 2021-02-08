package com.droidablebee.spock

import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator

class GooglePhotosSpec extends BaseSpockSpec {

    def "get all albums"() {

        expect:
        String token = getToken()
        List albums = getAllAlbums(token)
        int totalItems = albums.inject(0) { count, item -> count + Integer.valueOf(item['mediaItemsCount']) }
        List sorted = albums.sort({ Map album -> album.title })

        sorted.eachWithIndex { Map album, int index ->
            album.mediaItems = []

            println("Album: ${album.title}, items: ${album.mediaItemsCount} url: ${album.productUrl}")

            List items = getItemsForAlbum(album.id, token)
            println("\t total items received for album: ${items.size()}")
            if (items.size() != Integer.valueOf(album.mediaItemsCount)) {
                System.err.println("\t\t Warning: total items received does not match album media count")
            }

            album.mediaItems = items
        }
        println("Total albums: ${albums.size()} total album items: ${totalItems}")

        List items = getAllItems(token)
        println("Total media items: ${items.size()}")

        //map items
        Map itemsMap = items.collectEntries { Map item ->
            item.albums = []
            [item.id, item]
        }

        //add corresponding albums found for each media item
        albums.each { Map album ->
            album.mediaItems.each { Map item ->
                Map foundItem = itemsMap[item.id]
                if (foundItem) {
                    itemsMap[item.id].albums << album
                } else {
                    System.err.println("Warning: item: ${item} from album ${album.title}: ${album.productUrl} not found")
                }
            }
        }

        Map itemsWithoutAlbums = itemsMap.findAll { String key, Map item ->
            !item.albums
        }

        println("Media items w/out albums: ${itemsWithoutAlbums.size()}")
        itemsWithoutAlbums.each { String key, Map item ->
            println(item.productUrl)
        }
    }

    @Override
    protected String getDefaultUri() {
        return 'https://photoslibrary.googleapis.com/v1'
    }

    def getAllAlbums(String token) {

        int page = 0
        int pageSize = 50
        List albums = []
        String nextPageToken

        while (true) {
            print("Processing albums page: ${++page} ...")
            HttpResponseDecorator response = http.get(
                    requestContentType: ContentType.JSON,
                    path: getDefaultUri() + "/albums",
                    headers: [Authorization: "Bearer ${token}"],
                    query: [pageSize: pageSize, pageToken: nextPageToken]
            )

            assert response.status == 200
            List list = response.data['albums']
            println(" received: ${list.size()}")
            albums.addAll(list)
            nextPageToken = response.data['nextPageToken']

            if (!nextPageToken) {
                break
            }
        }

        return albums
    }

    def getItemsForAlbum(String albumId, String token) {

        int page = 0
        int pageSize = 100
        List items = []
        String nextPageToken

        while (true) {
            print("Processing album items page: ${++page} ...")
            HttpResponseDecorator response = http.post(
                    requestContentType: ContentType.JSON,
                    path: getDefaultUri() + "/mediaItems:search",
                    headers: [Authorization: "Bearer ${token}"],
                    body: [albumId: albumId, pageSize: pageSize, pageToken: nextPageToken]
            )

            assert response.status == 200
            List list = response.data['mediaItems']
            println(" received: ${list.size()}")
            items.addAll(list)
            nextPageToken = response.data['nextPageToken']

            if (!nextPageToken) {
                break
            }
        }

        return items
    }

    def getAllItems(String token) {

        int page = 0
        int pageSize = 100
        List items = []
        String nextPageToken

        while (true) {
            print("Processing media items page: ${++page} ...")
            HttpResponseDecorator response = http.get(
                    requestContentType: ContentType.JSON,
                    path: getDefaultUri() + "/mediaItems",
                    headers: [Authorization: "Bearer ${token}"],
                    query: [pageSize: pageSize, pageToken: nextPageToken]
            )

            assert response.status == 200
            List list = response.data['mediaItems']
            println(" received: ${list.size()}")
            items.addAll(list)
            nextPageToken = response.data['nextPageToken']

            if (!nextPageToken) {
                break
            }
        }

        return items
    }

    def getToken() {

        return System.getProperty("token")
    }
}
