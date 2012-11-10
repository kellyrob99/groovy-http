import org.kar.http.HotDeployedClass

def hotDeployed = new HotDeployedClass(name: 'First', other: 'Last')
html.html {
    body {
        p('hello world')
    }
}