package com.droidablebee.spock

import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import spock.lang.Unroll

class GitHubUserSpec extends BaseSpockSpec {

    @Unroll("user profile: #scenario")
    def "user profile"() {

        given: "github profile url"

        when: "http get is performed"

        HttpResponseDecorator response = http.get(
                requestContentType: ContentType.JSON,
                path: path
        )

        then: "expect http #httpStatus status"
        response.status == httpStatus

        where: "scenario #scenario"
        scenario                            | httpStatus | path
        "request profile for existing user" | 200        | '/users/pavelfomin'
        "request profile for invalid user"  | 404        | '/users/pavelfominthatdoesnotexist'
    }

    @Unroll("user profile details: #scenario")
    def "user profile details"() {

        given: "github profile url"

        when: "http get is performed"

        HttpResponseDecorator response = http.get(
                requestContentType: ContentType.JSON,
                path: path
        )

        then: "expect http #httpStatus status"
        response.status == httpStatus
        response.data['login'] == login
        response.data['id']
        response.data['url']
        response.data['type'] == type

        where: "scenario #scenario"
        scenario                         | httpStatus | path                | login        | type
        "request profile for pavelfomin" | 200        | '/users/pavelfomin' | 'pavelfomin' | 'User'
        "request profile for spring"     | 200        | '/users/spring'     | 'spring'     | 'Organization'
    }

    @Override
    protected String getDefaultUri() {
        return 'https://api.github.com'
    }
}
