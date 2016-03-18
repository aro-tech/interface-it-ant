# interface-it-ant
Custom Ant task for [interface_it] (https://github.com/aro-tech/interface-it)


##Latest release

Not released yet

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


##Roadmap and future directions:

###v0.7.0

 * Running implementation based on interface-it 0.7.0
 

##Blog
[![The Green Bar](https://img.shields.io/badge/My_Blog:-The_Green_Bar-brightgreen.svg)](https://thegreenbar.wordpress.com/)
