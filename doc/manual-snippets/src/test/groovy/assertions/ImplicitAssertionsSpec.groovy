/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package assertions

import geb.test.GebSpecWithCallbackServer
import geb.waiting.WaitTimeoutException

@SuppressWarnings("TrailingWhitespace")
class ImplicitAssertionsSpec extends GebSpecWithCallbackServer {

    String removeAsciidoctorTags(String withTags) {
        def lines = withTags.readLines()
        def filtered = lines[2..-2]
        filtered.join("\n")
    }

    String getExpectedAtCheckerFailureMessage() {
        removeAsciidoctorTags """
// tag::at_checker_message[]
Assertion failed: 

title == "Implicit Assertions!"
|     |
|     false
'Something else'
// end::at_checker_message[]
"""
    }

    String getExpectedAtCheckerWithAssertionsFailureMessage() {
        removeAsciidoctorTags """
// tag::at_checking_with_assertions_message[]
Assertion failed: 

headingText.empty
|           |
|           false
'This is a heading'
// end::at_checking_with_assertions_message[]
"""
    }

    String getExpectedWaitingFailureMessage() {
        removeAsciidoctorTags """
// tag::waiting_message[]
Assertion failed: 

title == "Page Title"
|     |
|     false
'Something else'
// end::waiting_message[]
"""
    }

    void stacktraceContains(Throwable e, String message) {
        def stringWriter = new StringWriter()
        def printWriter = new PrintWriter(stringWriter)
        e.printStackTrace(printWriter)
        assert stringWriter.toString().contains(message)
    }

    def "at checking implicit assertion failure message"() {
        given:
        html {
            title "Something else"
        }

        when:
        // tag::at_checker[]
        to ImplicitAssertionsExamplePage
        // end::at_checker[]

        then:
        AssertionError e = thrown()
        stacktraceContains(e, expectedAtCheckerFailureMessage)
    }

    def "at checking with assertions failure message"() {
        given:
        html {
            title "Implicit Assertions!"
            body {
                h1 "This is a heading"
            }
        }

        when:
        // tag::at_checking_with_assertions[]
        at(ImplicitAssertionsExamplePage) {
            headingText.empty
        }
        // end::at_checking_with_assertions[]

        then:
        AssertionError e = thrown()
        stacktraceContains(e, expectedAtCheckerWithAssertionsFailureMessage)
    }

    def "waiting implicit assertion failure message"() {
        given:
        html {
            title "Something else"
        }

        when:
        config.rawConfig.waiting.timeout = 0.1
        //tag::waiting[]
        waitFor { title == "Page Title" }
        //end::waiting[]

        then:
        WaitTimeoutException e = thrown()
        stacktraceContains(e, expectedWaitingFailureMessage)
    }

    def "selectively disabling implicit assertions"() {
        when:
        //tag::disable_implicit_assertions[]
        waitFor(implicitAssertions: false) {
            falseReturningMethod()
            true
        }
        //end::disable_implicit_assertions[]

        then:
        noExceptionThrown()
    }

    boolean falseReturningMethod() {
        false
    }
}
