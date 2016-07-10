# interface-it-ant
Custom Ant task for [interface_it] (https://github.com/aro-tech/interface-it)

Requires Java 8 (or higher)


##Latest release

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.aro-tech/interface-it-ant/badge.svg)](http://search.maven.org/#artifactdetails|com.github.aro-tech|interface-it-ant|1.1.3|jar)

v1.1.3: [Release notes on github] (https://github.com/aro-tech/interface-it-ant/releases/tag/v1.1.3)

[Binary downloads] (https://oss.sonatype.org/content/groups/public/com/github/aro-tech/interface-it-ant/1.1.3/ "binaries")


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

##Tag Attributes
 * **outputSourceRootDirectory** - The root of the source tree where the generated interface's .java file will be written in it's package directory. (required)
 * **delegateClass** - fully-qualified class name of the delegate class to be wrapped (required)
 * **targetInterfaceName** - The name of the generated interface (optional - defaults to the short name of the delegate class)  
 * **targetPackageName** - Package of the generated interface (recommended - defaults to root package) 
 * **sourceTextFilePath** - Path of a .java or .txt file containing method signatures to be read to recover argument names not retained by the compiler (optional - should not be used in the same tag as sourceArchivePath)
 * **sourceArchivePath** - Path of a .jar or .zip file containing source files in the package hierarchy with method signatures to be read to recover argument names not retained by the compiler (optional - should not be used in the same tag as sourceTextFilePath)
 * **ignoreDeprecated** - true or false - if true, deprecated methods will be ignored, if false deprecated delegate calls to them will be generated (optional - defaults to false) 
 * **targetInterfaceParentName** - If this attribute is non-empty, the task will build 2 separate mixins, one for the *delegateClass* itself and one for its superclass. The mixin for the *delegateClass* will extend the mixin generated for its superclass. (optional - default behavior is to build one mixin which includes any superclass methods)
 
##Blog
[![The Green Bar](https://img.shields.io/badge/My_Blog:-The_Green_Bar-brightgreen.svg)](https://thegreenbar.wordpress.com/)
