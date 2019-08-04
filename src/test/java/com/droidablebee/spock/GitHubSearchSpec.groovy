package com.droidablebee.spock

import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import spock.lang.Unroll

class GitHubSearchSpec extends BaseSpockSpec {

    @Unroll("Search users: #scenario")
    def "Search users"() {

        given: "github search url"

        when: "http get is performed"

        HttpResponseDecorator response = http.get(
                requestContentType: ContentType.JSON,
                query: query
        )

        then: "expect http #httpStatus status"
        response.status == httpStatus

        where: "scenario #scenario"
        scenario                   | httpStatus | query
        "search for existing user" | 200        | [q: 'user:pavelfomin']
        "search for invalid user"  | 422        | [q: 'user:pavelfominthatdoesnotexist']
    }

    @Override
    protected String getDefaultUri() {
        return 'https://api.github.com/search/users'
    }
}
