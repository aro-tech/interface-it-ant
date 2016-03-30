# interface-it-ant
Custom Ant task for [interface_it] (https://github.com/aro-tech/interface-it)

Requires Java 8 (or higher)


##Latest release

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.aro-tech/interface-it-ant/badge.svg)](http://search.maven.org/#artifactdetails|com.github.aro-tech|interface-it-ant|0.8.1|jar)

v0.8.1: [Release notes on github] (https://github.com/aro-tech/interface-it-ant/releases/tag/v0.8.1)

[Binary downloads] (https://oss.sonatype.org/content/groups/public/com/github/aro-tech/interface-it-ant/0.8.1/ "binaries")

##Example usage in a build.xml file

```xml
<interface-it echo="Trying to wrap AssertJ"  debug="true"
			outputSourceRootDirectory="${output.java.root}" 
			delegateClass="org.assertj.core.api.Assertions" 
			targetInterfaceName="AssertJ" 
			targetPackageName="${target.package}"
			sourceArchivePath="${assertj.sources.jar.path}"
		/>
```
		
See the [full example code here](https://github.com/aro-tech/interface-it-ant/blob/master/examples/build.xml "full example code").

 

##Blog
[![The Green Bar](https://img.shields.io/badge/My_Blog:-The_Green_Bar-brightgreen.svg)](https://thegreenbar.wordpress.com/)
