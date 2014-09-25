grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        runtime 'dom4j:dom4j:1.6.1', {
            excludes 'jaxen', 'jaxme-api', 'junitperf', 'pull-parser', 'relaxngDatatype', 'stax-api',
                     'stax-ri', 'xalan', 'xercesImpl', 'xpp3', 'xsdlib', 'xml-apis'
        }
    }

    plugins {
        build ':release:2.2.1', ':rest-client-builder:1.0.3', {
            export = false
        }

        runtime( ":jquery:1.7.1" ) {
            export = false
        }

        runtime( ":hibernate:$grailsVersion" ) {
            export = false
        }
    }
}
