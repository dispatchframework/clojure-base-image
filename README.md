# clojure-base-image
Clojure (on JVM) support for Dispatch

Latest image [on Docker Hub](https://hub.docker.com/r/dispatchframework/clojure-base/tags/)

## Usage

You need a recent version of Dispatch [installed in your Kubernetes cluster, Dispatch CLI configured](https://vmware.github.io/dispatch/documentation/guides/quickstart) to use it.

### Adding the Base Image

To add the base-image to Dispatch:
```bash
$ dispatch create base-image clj dispatchframework/clojure-base:<tag>
```

Make sure the base-image status is `READY` (it normally goes from `INITIALIZED` to `READY`):
```bash
$ dispatch get base-image clj
```

### Adding Runtime Dependencies

Library dependencies listed in `deps.edn` ([Clojure dependency manifest](https://clojure.org/guides/deps_and_cli)) need to be wrapped into a Dispatch image. For example, suppose we need a YAML parser and an image processing library:

```bash
$ cat ./deps.edn
```
```clojure
{:deps
 {exoscale/clj-yaml {:mvn/version "0.5.6"}
  net.mikera/imagez {:mvn/version "0.12.0"}}}
```
```bash
$ dispatch create image clj-mylibs clj --runtime-deps ./deps.edn
```

Make sure the image status is `READY` (it normally goes from `INITIALIZED` to `READY`):
```bash
$ dispatch get image clj-mylibs
```


### Creating Functions

Using the Clojure base-image, you can create Dispatch functions from Clojure project directories (source files are in `src` sub-dir): 

```bash
$ cat ./src/func_demo.clj
```
```clojure
(ns func-demo
  (:require [clj-yaml.core :as yaml]))

(defn parse-yaml [contextpayload]
  (yaml/parse-string payload))
```  
```bash
$ dispatch create function --image=clj-mylibs parse-yaml . --handler=func-demo/parse-yaml
```

You can also use a single source file to create a simple function. The only requirement is: the entry point (handler) function must be a public function named **`handle`** that accepts 2 arguments (`context` and `payload`), for example:  

```bash
$ cat ./src/func_demo.clj
```
```clojure
(ns func-demo
  (:require [clj-yaml.core :as yaml]))

(defn handle [context payload]
  (yaml/parse-string payload))
```

```bash
$ dispatch create function --image=clj-mylibs parse-yaml ./src/func_demo.clj
```


Make sure the function status is `READY` (it normally goes from `INITIALIZED` to `READY`):
```bash
$ dispatch get function parse-yaml
```

### Running Functions

As usual:

```bash
$ dispatch exec --json --input '"{name: VMware, place: Palo Alto}"' --wait parse-yaml
```
```json
{
    "blocking": true,
    "executedTime": 1524535747,
    "faasId": "5e6dde4a-9ac8-4b4e-80c0-5166f0a3b3c3",
    "finishedTime": 1524535756,
    "functionId": "486de3c7-b428-400e-8cc8-483f1955b627",
    "functionName": "parse-yaml",
    "input": "{name: VMware, place: Palo Alto}",
    "logs": null,
    "name": "7a8e76ab-d3c3-4a83-ba69-735d984c50af",
    "output": {
        "name": "VMware",
        "place": "Palo Alto"
    },
    "reason": null,
    "secrets": [],
    "services": null,
    "status": "READY",
    "tags": []
}
```
