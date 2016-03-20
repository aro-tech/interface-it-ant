# interface-it-ant
Custom Ant task for [interface_it] (https://github.com/aro-tech/interface-it)


##Latest release

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.aro-tech/interface-it-ant/badge.svg)](http://search.maven.org/#artifactdetails|com.github.aro-tech|interface-it-ant|0.7.0|jar)

v0.7.0: [Release notes on github] (https://github.com/aro-tech/interface-it-ant/releases/tag/0.7.0)

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
