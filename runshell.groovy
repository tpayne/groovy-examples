//
// Groovy script for running shell commands
//

def cmdStr = "(/bin/rm -f /tmp/test.log; echo test; date; ls) > /tmp/test.log 2>&1"

static def runCmd(cmdStr) {
	ProcessBuilder ph = new ProcessBuilder("sh","-c",cmdStr)
	ph.redirectErrorStream(true);
	Process shell = ph.start()
	shell.waitFor()
        if (shell.exitValue()>0) {
		println "Exit text is ${shell.text}"
	}
}

runCmd(cmdStr)
runCmd("(echo This is a test; date; ls foo.log)")
runCmd("echo This is a command")
