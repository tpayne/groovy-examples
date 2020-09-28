//
// Groovy script for dumping metadata from AWS URL
//
import groovy.json.*
import groovy.xml.*

class AWSMetaDump {
    // AWS metadata URI...
    static def urlString = "http://169.254.169.254/latest/meta-data/"

    //
    // Read the data from the URI specified
    //
    static def readURI(def uri) {
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

    // Recurse map to find value
    static def recurseFind(def map, def key) {
        if (map != null) {
            if (map.containsKey(key)) return map[key]
            map.findResult { k, v -> v instanceof Map ? recurseFind(v, key) : null }
        } else {
            return null
        }
    }

    // Main routine
    static void main(String[] args) { 
        def uri = urlString
        def queryStr = null

        if (args.size()>0) {
        	for(int n=0; n<args.size(); n++) {
        		if (args[n].equals("--url")) {
					uri = args[n+1]
				} else if (args[n].equals("--query-key")) {
					queryStr = args[n+1]
				}
        	}
        }
        
        // Get the AWS metadata in collasped form...
        def output = scanAWSInstanceData(uri)

        if (queryStr!=null) {
            // Tokenize composite key...
            def tokens = queryStr.tokenize("/")
            def mm = output

            // Recurse map to find value...
            tokens.each { key -> 
                mm = recurseFind(mm, key)
            }   
            output = mm         
        }
        
        // Print the data out in JSON formats...
        if (output != null) {
            println "RAW JSON -> " + convertListToJSON(output)
            println "PRETTY JSON -> " + convertListToJSON(output,true)
        } else {
            println "No match found"
        }
    }
}
