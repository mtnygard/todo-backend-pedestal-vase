# Objective

Build a microservice using Cognitect's Clojure technologies:

* Vase - a data driven microservices container
* [Pedestal][pedestal] - a web stack
* [Datomic][datomic] - a consistent, relational database

All of this running as a unikernel on [OSv][osv]

[pedestal]: https://www.github.com/pedestal/pedestal-service

[datomic]: https://www.datomic.com

[osv]: https://osv.io

# Starting point

Developer laptop:
* MacOS 10.10.4
* Oracle JDK 1.8.0_45
* Oracle VirtualBox 4.3.28r100309
* Homebrew

I also have VMWare Fusion 7 installed. That may make a difference later.

# Install OSv

It only builds on Linux, and doesn't recognize Mint as a supported
distribution (even though the parent Ubuntu distro is supported.) So
I'll use a pre-built binary.

Installing per the [Run Locally](http://osv.io/run-locally/)
instructions.

```
curl https://raw.githubusercontent.com/cloudius-systems/capstan/master/scripts/download | bash
```

No output, but I now have `capstan` in my `~/bin` directory.

Smoke test

```
capstan rumicro-ledger master mn ⚡ › capstan run cloudius/osv
Downloading cloudius/osv/index.yaml...
170 B / 170 B [=========================================================] 100.00 %
Downloading cloudius/osv/osv.vbox.gz...
21.80 MB / 21.80 MB [===================================================] 100.00 %
Created instance: cloudius-osv

dial unix /Users/mtnygard/.capstan/instances/vbox/cloudius-osv/cloudius-osv.sock: no such file or directory
```

Not a good start.

Others are reporting the
[same problem](https://github.com/cloudius-systems/capstan/issues/137).

Seems like qemu is needed for image creation and may help that error.

```
brew install qemu
```

If I run the image directly from VirtualBox, it works fine. (Creates
the .sock file and everything.)

Apparently there are
[better install instructions](https://github.com/cloudius-systems/capstan/wiki/Capstan-Installation)
in the
[capstan wiki](https://github.com/cloudius-systems/capstan/wiki).

I use ~/.bash_profile, so I'll modify the commands to suit.

```
$ rm ~/bin/capstan
$ rm -r ~/.capstan
$ brew install go qemu git
$ echo 'export GOPATH=$HOME/go'    >>  $HOME/.bash_profile
$ echo 'export PATH=$PATH:$GOPATH/bin'  >> $HOME/.bash_profile
$ source  $HOME/.bash_profile
$ go get github.com/cloudius-systems/capstan
$ ls ~/go/
bin pkg src
$ ls ~/go/bin
capstan
$ which capstan
/Users/mtnygard/go/bin/capstan
$ capstan run cloudius/osv
Downloading cloudius/osv/index.yaml...
170 B / 170 B [==============================================================================================================] 100.00 % 0
170 B / 170 B [==============================================================================================================] 100.00 % 0Downloading cloudius/osv/osv.vbox.gz...
21.80 MB / 21.80 MB [=======================================================================================================] 100.00 % 6s
Created instance: cloudius-osv

dial unix /Users/mtnygard/.capstan/instances/vbox/cloudius-osv/cloudius-osv.sock: no such file or directory
```

Discovered I was running VirtualBox 4.3.28. Found out that 4.3.30 was
the latest. Updated to 4.3.30 and it works great!

```
capstan run cloudius/osv
Created instance: cloudius-osv
OSv v0.22
eth0: 10.0.2.15
pthread_setcancelstate() stubbed
^[[40;1R/#
```



# Make a starter service

## Use a Leiningen Template

We're going to use [Paul deGrandis's](https://www.github.com/ohpauleez)
version of the
[pedestal-micro](https://github.com/ohpauleez/pedestal-micro)
template.

```
$ cd ~/work
$ git clone git@github.com:ohpauleez/pedestal-micro.git
Cloning into 'pedestal-micro'...
remote: Counting objects: 251, done.
remote: Total 251 (delta 0), reused 0 (delta 0), pack-reused 251
Receiving objects: 100% (251/251), 38.91 KiB | 0 bytes/s, done.
Resolving deltas: 100% (99/99), done.
Checking connectivity... done.
$ cd pedestal-micro
$ lein install
Created /Users/mtnygard/work/pedestal-micro/target/lein-template-0.3.0-Pd.jar
Wrote /Users/mtnygard/work/pedestal-micro/pom.xml
Installed jar and pom into local repo.
```


Add Paul's template to my Lein [profile](https://github.com/technomancy/leiningen/blob/master/doc/PROFILES.md).

``` ~/.lein/profiles.clj
{:user {:plugins      [[cider/cider-nrepl "0.9.1"]
                       [com.jakemccrary/lein-test-refresh "0.10.0"]
                       [pedestal-micro/lein-template "0.3.0-Pd"]
                       [refactor-nrepl    "1.1.0-SNAPSHOT"]]}}
```

Create the project

```
$ cd ../micro-ledger
$ lein new pedestal-micro vase-osv
Generating fresh 'lein new' pedestal-micro project.
$ ls
README.md    lab-notes.md vase-osv
```

Smoke test the new service:

```
$ capstan run
Building vase-osv...
Downloading cloudius/osv-openjdk8/index.yaml...
180 B / 180 B [====================================================================================] 100.00 % 0
180 B / 180 B [====================================================================================] 100.00 % 0Downloading cloudius/osv-openjdk8/osv-openjdk8.vbox.gz...
68.93 MB / 68.93 MB [============================================================================] 100.00 % 25s
./target/vase-osv-0.0.1-SNAPSHOT-standalone.jar: no such file or directory
```

Oh, right. I need to build the jar first.

```
$ lein uberjar
Retrieving lein-marginalia/lein-marginalia/0.8.0/lein-marginalia-0.8.0.pom from clojars
Retrieving marginalia/marginalia/0.8.0/marginalia-0.8.0.pom from clojars
Retrieving org/clojure/clojurescript/0.0-2138/clojurescript-0.0-2138.pom from central
Retrieving org/clojure/tools.cli/0.2.1/tools.cli-0.2.1.pom from central
Retrieving org/markdownj/markdownj/0.3.0-1.0.2b4/markdownj-0.3.0-1.0.2b4.pom from central
Retrieving de/ubercode/clostache/clostache/1.3.1/clostache-1.3.1.pom from clojars
Retrieving codox/codox/0.8.12/codox-0.8.12.pom from clojars
Retrieving codox/codox.leiningen/0.8.12/codox.leiningen-0.8.12.pom from clojars
Retrieving lein-cljfmt/lein-cljfmt/0.1.10/lein-cljfmt-0.1.10.pom from clojars
Retrieving cljfmt/cljfmt/0.1.10/cljfmt-0.1.10.pom from clojars
Retrieving com/googlecode/java-diff-utils/diffutils/1.2.1/diffutils-1.2.1.pom from central
Retrieving org/clojure/clojurescript/0.0-2138/clojurescript-0.0-2138.jar from central
Retrieving org/clojure/google-closure-library/0.0-20130212-95c19e7f0f5f/google-closure-library-0.0-20130212-95c19e7f0f5f.jar from central
Retrieving com/google/protobuf/protobuf-java/2.4.1/protobuf-java-2.4.1.jar from central
Retrieving org/clojure/google-closure-library-third-party/0.0-20130212-95c19e7f0f5f/google-closure-library-third-party-0.0-20130212-95c19e7f0f5f.jar from central
Retrieving org/clojure/tools.cli/0.2.1/tools.cli-0.2.1.jar from central
Retrieving org/markdownj/markdownj/0.3.0-1.0.2b4/markdownj-0.3.0-1.0.2b4.jar from central
Retrieving com/googlecode/java-diff-utils/diffutils/1.2.1/diffutils-1.2.1.jar from central
Retrieving de/ubercode/clostache/clostache/1.3.1/clostache-1.3.1.jar from clojars
Retrieving lein-cljfmt/lein-cljfmt/0.1.10/lein-cljfmt-0.1.10.jar from clojars
Retrieving codox/codox.leiningen/0.8.12/codox.leiningen-0.8.12.jar from clojars
Retrieving codox/codox/0.8.12/codox-0.8.12.jar from clojars
Retrieving lein-marginalia/lein-marginalia/0.8.0/lein-marginalia-0.8.0.jar from clojars
Retrieving marginalia/marginalia/0.8.0/marginalia-0.8.0.jar from clojars
Retrieving cljfmt/cljfmt/0.1.10/cljfmt-0.1.10.jar from clojars
Retrieving com/cognitect/transit-clj/0.8.271/transit-clj-0.8.271.pom from central
Retrieving com/cognitect/transit-java/0.8.287/transit-java-0.8.287.pom from central
Retrieving io/rkn/conformity/0.3.4/conformity-0.3.4.pom from clojars
Retrieving net/openhft/chronicle-logger-logback/1.1.0/chronicle-logger-logback-1.1.0.pom from central
Retrieving net/openhft/chronicle-logger-parent/1.1.0/chronicle-logger-parent-1.1.0.pom from central
Retrieving net/openhft/java-parent-pom/1.1.4/java-parent-pom-1.1.4.pom from central
Retrieving net/openhft/root-parent-pom/1.1.1/root-parent-pom-1.1.1.pom from central
Retrieving net/openhft/third-party-bom/3.4.18/third-party-bom-3.4.18.pom from central
Retrieving net/openhft/chronicle-logger/1.1.0/chronicle-logger-1.1.0.pom from central
Retrieving net/openhft/affinity/2.2/affinity-2.2.pom from central
Retrieving net/openhft/java-parent-pom/1.1.2/java-parent-pom-1.1.2.pom from central
Retrieving org/kohsuke/jetbrains/annotations/9.0/annotations-9.0.pom from central
Retrieving net/openhft/chronicle/3.4.2/chronicle-3.4.2.pom from central
Retrieving net/openhft/lang/6.6.2/lang-6.6.2.pom from central
Retrieving org/ow2/asm/asm/5.0.3/asm-5.0.3.pom from central
Retrieving org/ow2/asm/asm-parent/5.0.3/asm-parent-5.0.3.pom from central
Retrieving net/openhft/compiler/2.2.0/compiler-2.2.0.pom from central
Retrieving net/openhft/java-parent-pom/1.0.1/java-parent-pom-1.0.1.pom from central
Retrieving net/openhft/root-parent-pom/1.0.1/root-parent-pom-1.0.1.pom from central
Retrieving net/openhft/third-party-bom/3.4.0/third-party-bom-3.4.0.pom from central
Retrieving net/openhft/root-parent-pom/1.1.0/root-parent-pom-1.1.0.pom from central
Retrieving org/xerial/snappy/snappy-java/1.1.1.6/snappy-java-1.1.1.6.pom from central
Retrieving org/slf4j/jul-to-slf4j/1.7.6/jul-to-slf4j-1.7.6.pom from central
Retrieving org/slf4j/jul-to-slf4j/1.7.12/jul-to-slf4j-1.7.12.pom from central
Retrieving org/slf4j/jcl-over-slf4j/1.7.12/jcl-over-slf4j-1.7.12.pom from central
Retrieving org/slf4j/log4j-over-slf4j/1.7.12/log4j-over-slf4j-1.7.12.pom from central
Retrieving com/cognitect/transit-clj/0.8.271/transit-clj-0.8.271.jar from central
Retrieving net/openhft/chronicle-logger/1.1.0/chronicle-logger-1.1.0.jar from central
Retrieving com/cognitect/transit-java/0.8.287/transit-java-0.8.287.jar from central
Retrieving org/clojure/tools.namespace/0.2.4/tools.namespace-0.2.4.jar from central
Retrieving net/openhft/chronicle-logger-logback/1.1.0/chronicle-logger-logback-1.1.0.jar from central
Retrieving org/kohsuke/jetbrains/annotations/9.0/annotations-9.0.jar from central
Retrieving net/openhft/lang/6.6.2/lang-6.6.2.jar from central
Retrieving net/openhft/chronicle/3.4.2/chronicle-3.4.2.jar from central
Retrieving net/openhft/affinity/2.2/affinity-2.2.jar from central
Retrieving net/openhft/compiler/2.2.0/compiler-2.2.0.jar from central
Retrieving org/ow2/asm/asm/5.0.3/asm-5.0.3.jar from central
Retrieving org/xerial/snappy/snappy-java/1.1.1.6/snappy-java-1.1.1.6.jar from central
Retrieving org/slf4j/jul-to-slf4j/1.7.12/jul-to-slf4j-1.7.12.jar from central
Retrieving org/slf4j/jcl-over-slf4j/1.7.12/jcl-over-slf4j-1.7.12.jar from central
Retrieving org/slf4j/log4j-over-slf4j/1.7.12/log4j-over-slf4j-1.7.12.jar from central
Retrieving ns-tracker/ns-tracker/0.2.2/ns-tracker-0.2.2.jar from clojars
Retrieving io/rkn/conformity/0.3.4/conformity-0.3.4.jar from clojars
Compiling vase-osv
Reflection warning, clojure/core/incubator.clj:90:7 - reference to field getClass can't be resolved.
Reflection warning, clojure/core/incubator.clj:90:7 - reference to field isArray can't be resolved.
Boxed math warning, clojure/core/async/impl/channels.clj:117:41 - call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/core/async/impl/channels.clj:200:22 - call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/core/async/impl/timers.clj:48:23 - call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,long).
Boxed math warning, clojure/tools/analyzer/utils.clj:182:20 - call: public static boolean clojure.lang.Numbers.gte(java.lang.Object,long).
Boxed math warning, clojure/tools/analyzer.clj:677:30 - call: public static boolean clojure.lang.Numbers.lte(java.lang.Object,java.lang.Object).
Reflection warning, clojure/data/priority_map.clj:215:19 - call to method equiv on java.lang.Object can't be resolved (no such method).
Boxed math warning, clojure/core/cache.clj:146:38 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_minus(long,java.lang.Object).
Boxed math warning, clojure/core/cache.clj:150:30 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_minus(java.lang.Object,long).
Boxed math warning, clojure/core/cache.clj:174:28 - call: public static boolean clojure.lang.Numbers.gte(long,java.lang.Object).
Boxed math warning, clojure/core/cache.clj:200:23 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_minus(java.lang.Object,long).
Boxed math warning, clojure/core/cache.clj:200:61 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_minus(java.lang.Object).
Boxed math warning, clojure/core/cache.clj:213:17 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/core/cache.clj:219:17 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/core/cache.clj:220:11 - call: public static boolean clojure.lang.Numbers.gte(long,java.lang.Object).
Boxed math warning, clojure/core/cache.clj:237:20 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/core/cache.clj:251:33 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_minus(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/core/cache.clj:251:30 - call: public static boolean clojure.lang.Numbers.gt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/core/cache.clj:265:27 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_minus(java.lang.Object).
Boxed math warning, clojure/core/cache.clj:266:10 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_minus(long,java.lang.Object).
Boxed math warning, clojure/core/cache.clj:266:7 - call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/core/cache.clj:301:9 - call: public static boolean clojure.lang.Numbers.gte(long,java.lang.Object).
Boxed math warning, clojure/core/cache.clj:421:17 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/core/cache.clj:444:17 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/core/cache.clj:445:11 - call: public static boolean clojure.lang.Numbers.lt(long,java.lang.Object).
Boxed math warning, clojure/core/cache.clj:475:41 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_minus(java.lang.Object).
Boxed math warning, clojure/core/cache.clj:476:41 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_minus(java.lang.Object).
Boxed math warning, clojure/core/cache.clj:575:30 - call: public static boolean clojure.lang.Numbers.lt(long,java.lang.Object).
Boxed math warning, clojure/core/cache.clj:577:11 - call: public static boolean clojure.lang.Numbers.equiv(java.lang.Object,long).
Boxed math warning, clojure/core/cache.clj:587:30 - call: public static boolean clojure.lang.Numbers.lt(long,java.lang.Object).
Boxed math warning, clojure/core/cache.clj:598:24 - call: public static boolean clojure.lang.Numbers.lte(long,java.lang.Object).
Boxed math warning, clojure/core/cache.clj:608:30 - call: public static boolean clojure.lang.Numbers.lt(long,java.lang.Object).
Boxed math warning, clojure/core/cache.clj:618:36 - call: public static boolean clojure.lang.Numbers.lt(long,java.lang.Object).
Boxed math warning, clojure/core/cache.clj:619:36 - call: public static boolean clojure.lang.Numbers.lt(long,java.lang.Object).
Reflection warning, clojure/core/memoize.clj:72:23 - reference to field cache can't be resolved.
Boxed math warning, clojure/tools/reader/impl/utils.clj:24:9 - call: public static boolean clojure.lang.Numbers.gt(java.lang.Object,long).
Boxed math warning, clojure/tools/reader/reader_types.clj:50:11 - call: public static boolean clojure.lang.Numbers.gt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:52:9 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:55:11 - call: public static boolean clojure.lang.Numbers.gt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:81:10 - call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:83:10 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:88:10 - call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:94:11 - call: public static boolean clojure.lang.Numbers.isZero(java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:95:7 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_dec(java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:120:11 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:121:9 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:130:11 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_dec(java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:132:7 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_dec(java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:210:11 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:211:9 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:221:11 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_dec(java.lang.Object).
Boxed math warning, clojure/tools/reader/reader_types.clj:223:7 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_dec(java.lang.Object).
Boxed math warning, clojure/tools/reader/impl/commons.clj:78:5 - call: public static java.lang.Number clojure.lang.Numbers.divide(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/tools/reader/default_data_readers.clj:30:3 - call: public static boolean clojure.lang.Numbers.isZero(java.lang.Object).
Boxed math warning, clojure/tools/reader/default_data_readers.clj:45:9 - call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,long).
Boxed math warning, clojure/tools/reader/default_data_readers.clj:47:21 - call: public static boolean clojure.lang.Numbers.lt(long,java.lang.Object).
Boxed math warning, clojure/tools/reader/default_data_readers.clj:246:35 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_dec(java.lang.Object).
Boxed math warning, clojure/tools/reader/default_data_readers.clj:247:32 - call: public static java.lang.Number clojure.lang.Numbers.quotient(java.lang.Object,long).
Boxed math warning, clojure/tools/reader/default_data_readers.clj:250:32 - call: public static boolean clojure.lang.Numbers.isNeg(java.lang.Object).
Boxed math warning, clojure/tools/reader.clj:74:14 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_add(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/tools/reader.clj:75:18 - call: public static boolean clojure.lang.Numbers.equiv(long,java.lang.Object).
Boxed math warning, clojure/tools/reader.clj:78:14 - call: public static boolean clojure.lang.Numbers.equiv(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/tools/reader.clj:83:23 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/tools/reader.clj:83:42 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_multiply(long,java.lang.Object).
Boxed math warning, clojure/tools/reader.clj:83:37 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_add(long,java.lang.Object).
Boxed math warning, clojure/tools/reader.clj:89:18 - call: public static boolean clojure.lang.Numbers.equiv(long,java.lang.Object).
Boxed math warning, clojure/tools/reader.clj:102:46 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_multiply(long,java.lang.Object).
Boxed math warning, clojure/tools/reader.clj:102:41 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_add(long,java.lang.Object).
Boxed math warning, clojure/tools/reader.clj:153:33 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_dec(java.lang.Object).
Boxed math warning, clojure/tools/reader.clj:382:20 - call: public static boolean clojure.lang.Numbers.equiv(long,java.lang.Object).
Boxed math warning, clojure/tools/reader.clj:395:37 - call: public static boolean clojure.lang.Numbers.gt(long,java.lang.Object).
Boxed math warning, clojure/core/async/impl/ioc_macros.clj:837:33 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/core/async/impl/ioc_macros.clj:887:25 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_add(java.lang.Object,long).
Boxed math warning, clojure/core/async/impl/ioc_macros.clj:888:24 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_add(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/core/async.clj:443:14 - call: public static boolean clojure.lang.Numbers.isPos(java.lang.Object).
Boxed math warning, clojure/core/async.clj:578:5 - call: public static java.lang.Object clojure.lang.Numbers.min(java.lang.Object,long).
Boxed math warning, clojure/core/async.clj:580:18 - call: public static boolean clojure.lang.Numbers.lt(long,java.lang.Object).
Boxed math warning, clojure/core/async.clj:639:28 - call: public static boolean clojure.lang.Numbers.isZero(java.lang.Object).
Boxed math warning, clojure/core/async.clj:641:5 - call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/core/async.clj:641:5 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/core/async.clj:641:5 - call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/core/async.clj:641:5 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/core/async.clj:831:8 - call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/core/async.clj:831:8 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/core/async.clj:883:34 - call: public static boolean clojure.lang.Numbers.isZero(java.lang.Object).
Boxed math warning, clojure/core/async.clj:886:8 - call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/core/async.clj:886:8 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/core/async.clj:936:20 - call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/core/async.clj:940:27 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/core/async.clj:1032:3 - call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/core/async.clj:1032:3 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/core/async.clj:1086:37 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, clojure/core/async.clj:1087:29 - call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,java.lang.Object).
Boxed math warning, clojure/core/async.clj:1091:29 - call: public static boolean clojure.lang.Numbers.gt(java.lang.Object,long).
Boxed math warning, io/pedestal/http/route/prefix_tree.clj:31:13 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Boxed math warning, io/pedestal/http/route/prefix_tree.clj:204:47 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_inc(java.lang.Object).
Reflection warning, clj_time/core.clj:577:10 - reference to field getDayOfMonth on java.lang.Object can't be resolved.
Reflection warning, ring/util/request.clj:28:5 - call to java.lang.Long ctor can't be resolved.
Boxed math warning, crypto/equality.clj:11:7 - call: public static boolean clojure.lang.Numbers.isZero(java.lang.Object).
Boxed math warning, cheshire/generate.clj:48:16 - call: public static boolean clojure.lang.Numbers.lt(long,java.lang.Object).
Boxed math warning, cognitect/transit.clj:184:21 - call: public static java.lang.Number clojure.lang.Numbers.divide(java.lang.Object,java.lang.Object).
Boxed math warning, io/pedestal/http/route/definition/verbose.clj:174:51 - call: public static boolean clojure.lang.Numbers.gt(java.lang.Object,long).
Reflection warning, ns_tracker/core.clj:23:28 - reference to field lastModified can't be resolved.
Boxed math warning, ns_tracker/core.clj:28:3 - call: public static boolean clojure.lang.Numbers.gt(java.lang.Object,java.lang.Object).
Compiling vase-osv
Created /Users/mtnygard/work/micro-ledger/vase-osv/target/vase-osv-0.0.1-SNAPSHOT.jar
Created /Users/mtnygard/work/micro-ledger/vase-osv/target/vase-osv-0.0.1-SNAPSHOT-standalone.jar
```

Seems OK. Now to rerun that smoke test:

```
$ capstan run
Building vase-osv...
Uploading files...
1 / 1 [==========================================================] 100.00 % 0Created instance: vase-osv
OSv v0.22
eth0: 10.0.2.15
Failed to instantiate [ch.qos.logback.classic.LoggerContext]
Reported exception:
java.lang.ExceptionInInitializerError
	at net.openhft.chronicle.VanillaChronicle.<clinit>(VanillaChronicle.java:51)
	at net.openhft.chronicle.ChronicleQueueBuilder$VanillaChronicleQueueBuilder.cycleLength(ChronicleQueueBuilder.java:441)
	at net.openhft.chronicle.ChronicleQueueBuilder$VanillaChronicleQueueBuilder.cycleLength(ChronicleQueueBuilder.java:437)
	at net.openhft.chronicle.logger.VanillaLogAppenderConfig.build(VanillaLogAppenderConfig.java:164)
	at net.openhft.chronicle.logger.ChronicleLogWriters.text(ChronicleLogWriters.java:614)
	at net.openhft.chronicle.logger.logback.TextVanillaChronicleAppender.createWriter(TextVanillaChronicleAppender.java:46)
	at net.openhft.chronicle.logger.logback.AbstractChronicleAppender.start(AbstractChronicleAppender.java:115)
	at ch.qos.logback.core.joran.action.AppenderAction.end(AppenderAction.java:96)
	at ch.qos.logback.core.joran.spi.Interpreter.callEndAction(Interpreter.java:317)
	at ch.qos.logback.core.joran.spi.Interpreter.endElement(Interpreter.java:196)
	at ch.qos.logback.core.joran.spi.Interpreter.endElement(Interpreter.java:182)
	at ch.qos.logback.core.joran.spi.EventPlayer.play(EventPlayer.java:62)
	at ch.qos.logback.core.joran.GenericConfigurator.doConfigure(GenericConfigurator.java:149)
	at ch.qos.logback.core.joran.GenericConfigurator.doConfigure(GenericConfigurator.java:135)
	at ch.qos.logback.core.joran.GenericConfigurator.doConfigure(GenericConfigurator.java:99)
	at ch.qos.logback.core.joran.GenericConfigurator.doConfigure(GenericConfigurator.java:49)
	at ch.qos.logback.classic.util.ContextInitializer.configureByResource(ContextInitializer.java:77)
	at ch.qos.logback.classic.util.ContextInitializer.autoConfig(ContextInitializer.java:152)
	at org.slf4j.impl.StaticLoggerBinder.init(StaticLoggerBinder.java:85)
	at org.slf4j.impl.StaticLoggerBinder.<clinit>(StaticLoggerBinder.java:55)
	at org.slf4j.LoggerFactory.bind(LoggerFactory.java:141)
	at org.slf4j.LoggerFactory.performInitialization(LoggerFactory.java:120)
	at org.slf4j.LoggerFactory.getILoggerFactory(LoggerFactory.java:331)
	at org.slf4j.LoggerFactory.getLogger(LoggerFactory.java:283)
	at org.slf4j.LoggerFactory.getLogger(LoggerFactory.java:304)
	at datomic.impl.lucene.HybridDirectory.<clinit>(HybridDirectory.java:19)
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Class.java:264)
	at datomic.fulltext$loading__4958__auto__.invoke(fulltext.clj:4)
	at datomic.fulltext__init.load(Unknown Source)
	at datomic.fulltext__init.<clinit>(Unknown Source)
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Class.java:348)
	at clojure.lang.RT.classForName(RT.java:2154)
	at clojure.lang.RT.classForName(RT.java:2163)
	at clojure.lang.RT.loadClassForName(RT.java:2182)
	at clojure.lang.RT.load(RT.java:436)
	at clojure.lang.RT.load(RT.java:412)
	at clojure.core$load$fn__5448.invoke(core.clj:5866)
	at clojure.core$load.doInvoke(core.clj:5865)
	at clojure.lang.RestFn.invoke(RestFn.java:408)
	at clojure.core$load_one.invoke(core.clj:5671)
	at clojure.core$load_lib$fn__5397.invoke(core.clj:5711)
	at clojure.core$load_lib.doInvoke(core.clj:5710)
	at clojure.lang.RestFn.applyTo(RestFn.java:142)
	at clojure.core$apply.invoke(core.clj:632)
	at clojure.core$load_libs.doInvoke(core.clj:5749)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invoke(core.clj:632)
	at clojure.core$require.doInvoke(core.clj:5832)
	at clojure.lang.RestFn.invoke(RestFn.java:2422)
	at datomic.index$loading__4958__auto__.invoke(index.clj:4)
	at datomic.index__init.load(Unknown Source)
	at datomic.index__init.<clinit>(Unknown Source)
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Class.java:348)
	at clojure.lang.RT.classForName(RT.java:2154)
	at clojure.lang.RT.classForName(RT.java:2163)
	at clojure.lang.RT.loadClassForName(RT.java:2182)
	at clojure.lang.RT.load(RT.java:436)
	at clojure.lang.RT.load(RT.java:412)
	at clojure.core$load$fn__5448.invoke(core.clj:5866)
	at clojure.core$load.doInvoke(core.clj:5865)
	at clojure.lang.RestFn.invoke(RestFn.java:408)
	at clojure.core$load_one.invoke(core.clj:5671)
	at clojure.core$load_lib$fn__5397.invoke(core.clj:5711)
	at clojure.core$load_lib.doInvoke(core.clj:5710)
	at clojure.lang.RestFn.applyTo(RestFn.java:142)
	at clojure.core$apply.invoke(core.clj:632)
	at clojure.core$load_libs.doInvoke(core.clj:5749)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invoke(core.clj:632)
	at clojure.core$require.doInvoke(core.clj:5832)
	at clojure.lang.RestFn.invoke(RestFn.java:1523)
	at datomic.query$loading__4958__auto__.invoke(query.clj:4)
	at datomic.query__init.load(Unknown Source)
	at datomic.query__init.<clinit>(Unknown Source)
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Class.java:348)
	at clojure.lang.RT.classForName(RT.java:2154)
	at clojure.lang.RT.classForName(RT.java:2163)
	at clojure.lang.RT.loadClassForName(RT.java:2182)
	at clojure.lang.RT.load(RT.java:436)
	at clojure.lang.RT.load(RT.java:412)
	at clojure.core$load$fn__5448.invoke(core.clj:5866)
	at clojure.core$load.doInvoke(core.clj:5865)
	at clojure.lang.RestFn.invoke(RestFn.java:408)
	at clojure.core$load_one.invoke(core.clj:5671)
	at clojure.core$load_lib$fn__5397.invoke(core.clj:5711)
	at clojure.core$load_lib.doInvoke(core.clj:5710)
	at clojure.lang.RestFn.applyTo(RestFn.java:142)
	at clojure.core$apply.invoke(core.clj:632)
	at clojure.core$load_libs.doInvoke(core.clj:5749)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invoke(core.clj:632)
	at clojure.core$require.doInvoke(core.clj:5832)
	at clojure.lang.RestFn.invoke(RestFn.java:436)
	at datomic.api$loading__4958__auto__.invoke(api.clj:6)
	at datomic.api__init.load(Unknown Source)
	at datomic.api__init.<clinit>(Unknown Source)
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Class.java:348)
	at clojure.lang.RT.classForName(RT.java:2154)
	at clojure.lang.RT.classForName(RT.java:2163)
	at clojure.lang.RT.loadClassForName(RT.java:2182)
	at clojure.lang.RT.load(RT.java:436)
	at clojure.lang.RT.load(RT.java:412)
	at clojure.core$load$fn__5448.invoke(core.clj:5866)
	at clojure.core$load.doInvoke(core.clj:5865)
	at clojure.lang.RestFn.invoke(RestFn.java:408)
	at clojure.core$load_one.invoke(core.clj:5671)
	at clojure.core$load_lib$fn__5397.invoke(core.clj:5711)
	at clojure.core$load_lib.doInvoke(core.clj:5710)
	at clojure.lang.RestFn.applyTo(RestFn.java:142)
	at clojure.core$apply.invoke(core.clj:632)
	at clojure.core$load_libs.doInvoke(core.clj:5749)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invoke(core.clj:632)
	at clojure.core$require.doInvoke(core.clj:5832)
	at clojure.lang.RestFn.invoke(RestFn.java:457)
	at vase_osv.routes$loading__5340__auto____9556.invoke(routes.clj:1)
	at vase_osv.routes__init.load(Unknown Source)
	at vase_osv.routes__init.<clinit>(Unknown Source)
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Class.java:348)
	at clojure.lang.RT.classForName(RT.java:2154)
	at clojure.lang.RT.classForName(RT.java:2163)
	at clojure.lang.RT.loadClassForName(RT.java:2182)
	at clojure.lang.RT.load(RT.java:436)
	at clojure.lang.RT.load(RT.java:412)
	at clojure.core$load$fn__5448.invoke(core.clj:5866)
	at clojure.core$load.doInvoke(core.clj:5865)
	at clojure.lang.RestFn.invoke(RestFn.java:408)
	at clojure.core$load_one.invoke(core.clj:5671)
	at clojure.core$load_lib$fn__5397.invoke(core.clj:5711)
	at clojure.core$load_lib.doInvoke(core.clj:5710)
	at clojure.lang.RestFn.applyTo(RestFn.java:142)
	at clojure.core$apply.invoke(core.clj:632)
	at clojure.core$load_libs.doInvoke(core.clj:5749)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invoke(core.clj:632)
	at clojure.core$require.doInvoke(core.clj:5832)
	at clojure.lang.RestFn.invoke(RestFn.java:551)
	at vase_osv$loading__5340__auto____27.invoke(vase_osv.clj:1)
	at vase_osv__init.load(Unknown Source)
	at vase_osv__init.<clinit>(Unknown Source)
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Class.java:348)
	at clojure.lang.RT.classForName(RT.java:2154)
	at clojure.lang.RT.classForName(RT.java:2163)
	at clojure.lang.RT.loadClassForName(RT.java:2182)
	at clojure.lang.RT.load(RT.java:436)
	at clojure.lang.RT.load(RT.java:412)
	at clojure.core$load$fn__5448.invoke(core.clj:5866)
	at clojure.core$load.doInvoke(core.clj:5865)
	at clojure.lang.RestFn.invoke(RestFn.java:408)
	at clojure.lang.Var.invoke(Var.java:379)
	at vase_osv.<clinit>(Unknown Source)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:497)
	at io.osv.ContextIsolator.runMain(ContextIsolator.java:233)
	at io.osv.ContextIsolator.access$400(ContextIsolator.java:32)
	at io.osv.ContextIsolator$3.run(ContextIsolator.java:118)
Caused by: java.lang.NumberFormatException: For input string: "self"
	at java.lang.NumberFormatException.forInputString(NumberFormatException.java:65)
	at java.lang.Integer.parseInt(Integer.java:580)
	at java.lang.Integer.parseInt(Integer.java:615)
	at net.openhft.lang.Jvm.getProcessId0(Jvm.java:103)
	at net.openhft.lang.Jvm.<clinit>(Jvm.java:77)
	... 165 more
```

Ah, Chronicler attempts to resolve /proc/self to a canonical file
name, which it then parses as an integer. On a *nix system, or even a
Mac, /proc/self is a link to a virtual file. Under OSv, it's just a
file whose contents are interesting. The file _name_ doesn't
change. Of course, had I read `project.clj`, I would see Paul's note
to comment out Chronicler in `project.clj` and `config/logback.xml`.

I'm not going to fuss with the correct syntax for nested XML quotes,
so I'll just remove the whole block from `config/logback.xml`.

Having done that, let's try that smoke test one more time.

And I'm still getting the same exception, even though I can check the
uberjar and see that there's not one net.openhft class in there. Maybe I need to rebuild the image.

```
$ capstan build
uilding vase-osv...
Uploading files...
1 / 1 [===============================================================================================================================================] 100.00 % 0
$ capstan run
Created instance: vase-osv
OSv v0.22
eth0: 10.0.2.15
INFO  org.eclipse.jetty.util.log - Logging initialized @6238ms
INFO  org.eclipse.jetty.server.Server - jetty-9.2.z-SNAPSHOT
INFO  o.e.j.server.handler.ContextHandler - Started o.e.j.s.ServletContextHandler@5bf61a73{/,null,AVAILABLE}
INFO  o.e.jetty.server.ServerConnector - Started ServerConnector@5e917b03{HTTP/1.1}{0.0.0.0:8080}
INFO  org.eclipse.jetty.server.Server - Started @6982ms
```

Yes! It's running. Now I'll try to hit the server at http://10.0.2.15:8080/

No joy. No route to host. Let me try with port forwarding

```
$ capstan run -f 8080:8080
Created instance: vase-osv
OSv v0.22
eth0: 10.0.2.15
INFO  org.eclipse.jetty.util.log - Logging initialized @6308ms
INFO  org.eclipse.jetty.server.Server - jetty-9.2.z-SNAPSHOT
INFO  o.e.j.server.handler.ContextHandler - Started o.e.j.s.ServletContextHandler@3039ea85{/,null,AVAILABLE}
INFO  o.e.jetty.server.ServerConnector - Started ServerConnector@5723fef2{HTTP/1.1}{0.0.0.0:8080}
INFO  org.eclipse.jetty.server.Server - Started @6714ms
```

Now http://localhost:8080/ returns a happy "Hello, World!"

# Adding Vase

I'm following the [instructions](https://github.com/cognitect-labs/vase/blob/master/docs/adding_vase.mkd).

1. Add the Vase dependency to project.clj. Done.

Vase is not in Clojars or Maven Central yet. Need to install locally.

```
$ git clone git@github.com:cognitect-labs/vase.git
Cloning into 'vase'...
remote: Counting objects: 486, done.
remote: Total 486 (delta 0), reused 0 (delta 0), pack-reused 486
Receiving objects: 100% (486/486), 595.24 KiB | 0 bytes/s, done.
Resolving deltas: 100% (273/273), done.
Checking connectivity... done.
$ cd vase
$ lein install
Retrieving com/datomic/datomic-free/0.9.5153/datomic-free-0.9.5153.pom from clojars
Retrieving ohpauleez/themis/0.1.1/themis-0.1.1.pom from clojars
Retrieving com/fasterxml/jackson/core/jackson-databind/2.4.4/jackson-databind-2.4.4.pom from central
Retrieving com/fasterxml/jackson/core/jackson-annotations/2.4.0/jackson-annotations-2.4.0.pom from central
Retrieving com/fasterxml/jackson/datatype/jackson-datatype-json-org/2.4.4/jackson-datatype-json-org-2.4.4.pom from central
Retrieving org/apache/geronimo/bundles/json/20090211_1/json-20090211_1.pom from central
Retrieving org/apache/geronimo/bundles/bundles-parent/1.0/bundles-parent-1.0.pom from central
Retrieving org/apache/geronimo/genesis/genesis-java5-flava/2.0/genesis-java5-flava-2.0.pom from central
Retrieving org/apache/geronimo/genesis/genesis-default-flava/2.0/genesis-default-flava-2.0.pom from central
Retrieving org/apache/geronimo/genesis/genesis/2.0/genesis-2.0.pom from central
Retrieving org/apache/apache/6/apache-6.pom from central
Retrieving com/fasterxml/jackson/core/jackson-databind/2.4.4/jackson-databind-2.4.4.jar from central
Retrieving joda-time/joda-time/2.6/joda-time-2.6.jar from central
Retrieving com/fasterxml/jackson/core/jackson-annotations/2.4.0/jackson-annotations-2.4.0.jar from central
Retrieving com/fasterxml/jackson/datatype/jackson-datatype-json-org/2.4.4/jackson-datatype-json-org-2.4.4.jar from central
Retrieving org/apache/geronimo/bundles/json/20090211_1/json-20090211_1.jar from central
Retrieving com/datomic/datomic-free/0.9.5153/datomic-free-0.9.5153.jar from clojars
Retrieving ohpauleez/themis/0.1.1/themis-0.1.1.jar from clojars
Created /Users/mtnygard/work/vase/target/vase-0.1.0-SNAPSHOT.jar
Wrote /Users/mtnygard/work/vase/pom.xml
Installed jar and pom into local repo.
```

Back in my vase-osv, I need to use the snapshot revision for Vase.

The tutorial says the dependency is `io.pedestal/vase`, but the Vase
project.clj makes it `com.cognitect/vase`. I've submitted an
[issue](https://github.com/cognitect-labs/vase/issues/1), and changed
my project.clj to `com.cognitect/vase`

Had to add some exclusions to `io.pedestal/pedestal-service`. It has
some transitive dependencies on cheshire and jackson-core that
conflict with vase.


# Rename everything

I decided to redirect this toward a Todo-Backend service. That makes
it more relatable to other examples. It also means I can use it for
the back end of the
[Simulant Example](https://github.com/mtnygard/simulant-example)
project that I've got going.

- Rename top level directory to `micro-todo-backend`
- Rename repo to `micro-todo-backend`
- Update git remote
- Nothing in vase-osv needs to change.

# Define the Service with Vase

We'll build this in payload format and upsert it to a running
container.

- Start the container `capstan run -f 8080:8080`

- Lots of errors. Needed to fix requires and some of the vase example
(http instead of bootstrap.)
- Updating pedestal-service to 0.4.1-SNAPSHOT (built locally)
