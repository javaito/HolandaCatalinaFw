# Getting started

##Java

To use the latest version of HCJF you need to have java version 9 or higher installed in our operating system

 - [Ubuntu](http://www.webupd8.org/2015/02/install-oracle-java-9-in-ubuntu-linux.html)
 - [Linux](https://docs.oracle.com/javase/9/install/installation-jdk-and-jre-linux-platforms.htm#JSJIG-GUID-737A84E4-2EFF-4D38-8E60-3E29D1B884B8)
 - [Windows](https://docs.oracle.com/javase/9/install/installation-jdk-and-jre-microsoft-windows-platforms.htm#JSJIG-GUID-A7E27B90-A28D-4237-9383-A58B416071CA)
 - [Mac OS](https://docs.oracle.com/javase/9/install/installation-jdk-and-jre-macos.htm#JSJIG-GUID-F9183C70-2E96-40F4-9104-F3814A5A331F)

##Dependencies

After java is already configured in your operating system you have to add the dependencies jar in your class path so that the virtual machine finds the dependencies. Doing this work without using any tool for the administration of dependencies can be complicated since each dependency can contain sub-dependencies and it would be difficult to find each of them. I recommend using [maven](https://maven.apache.org/guides/getting-started/index.html) for this task, just add the following code in your pom.xml file
```xml
<dependency>
    <groupId>com.github.javaito</groupId>
    <artifactId>hcjf</artifactId>
    <version>1.3.43</version>
</dependency>
```