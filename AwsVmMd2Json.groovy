//
// Groovy script for dumping metadata from AWS URL
//
import groovy.json.*
import groovy.xml.*

class AWSMetaDump {
    // AWS metadata URI
    static def urlString = "http://169.254.169.254/latest/meta-data/"

    //
    // Read the data from the URI specified
    //
    static def readURI(def uri) {
        def inUri = new URI(uri.toString())
        def outTxt

        try {
            outTxt = new URL(uri.toString()).getText()      
            outTxt = outTxt.replaceAll("\n"," ")
        } catch(Exception e) {
        }
        return outTxt
    }

    //
    // Scan the AWS metadata
    //
    static def scanAWSInstanceData(def uri) {
        def uriTxt = readURI(uri)
        def uriMap = [:]

        def tokens = uriTxt.tokenize(' ')
        tokens.each{ token ->
            if (token[token.length()-1] == '/') {
                // If the metadata has sub-keys then these need to be
                // scanned as well...
                def key = token[0..token.length()-2]
                uriMap[key] = scanAWSInstanceData(uri + token)
            } else {
                // No sub-keys, just get the value...
                uriMap[token] = readURI(uri + token)
            }
        }
        return uriMap
    }

    //
    // Pass the structure to Groovy JSON builder to convert to JSON
    //
    static String convertListToJSON(def conv, def pretty=false) {
        def builder = new JsonBuilder(conv)
        return (pretty) ? builder.toPrettyString() : builder.toString()
    }

    static void main(String[] args) { 
        def uri = urlString

        if (args.size()>0) {
            uri = args[0]
        }
        
        def output = scanAWSInstanceData(uri)
        println "RAW JSON -> " + convertListToJSON(output)
        println "PRETTY JSON -> " + convertListToJSON(output,true)
    }
}
