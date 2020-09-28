//
// Groovy script for getting json map key value
//
import groovy.json.*
import groovy.xml.*

class JsonMapper {

    // Pass the string to Groovy JSON slurper to convert to map
    static def convertJsonToMap(def json) {
        def slurp = new JsonSlurper()
        return slurp.parseText(json)
    }

    // Recurse map to find value
    static def recurseFind(def map, def key) {
        if (map.containsKey(key)) return map[key]
        map.findResult { k, v -> v instanceof Map ? recurseFind(v, key) : null }
    }

    // Pass the structure to Groovy JSON builder to convert to JSON
    static String convertListToJSON(def conv, def pretty=false) {
        def builder = new JsonBuilder(conv)
        return (pretty) ? builder.toPrettyString() : builder.toString()
    }

    // Main routine
    static void main(String[] args) { 
        def keyTxt
        def txt = null
        def file = null

        // Check args...
        if (args.size()>0) {
            for(int n=0; n<args.size(); n++) {
                if (args[n].equals("--json-key")) {
                    keyTxt = args[n+1]
                } else if (args[n].equals("--json-text")) {
                    txt = args[n+1]
                } else if (args[n].equals("--json-file")) {
                    file = args[n+1]
                }
            }
        }
        
        if (file != null) {
            txt = new File(file.toString()).text
        }

        // Convert JSON to map...
        def map = convertJsonToMap(txt)

        // Tokenize composite key...
        def tokens = keyTxt.tokenize("/")
        def mm = map

        // Recurse map to find value...
        tokens.each { key -> 
            mm = recurseFind(mm, key)
        }

        if (mm==null) {
            println "No match found"
        } else {
            println "Result='" + convertListToJSON(mm,true) + "'"
        }
    }
}
