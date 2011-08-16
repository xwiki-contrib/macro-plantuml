PlantUML Macro is a XWiki Rendering Macro written in wiki pages, 
using velocity and groovy.
Read this if you want to know more about that:
http://platform.xwiki.org/xwiki/bin/view/DevGuide/WikiMacroTutorial

This macro contains 3 wiki pages:
- XWiki.PlantUMLMacro is the main page containing the WikiMacroClass and 
  the PlantUMLConfigurationClass objects.
- XWiki.PlantUMLConfigurationClass is the definition of the class containing
  the configuration data.
- XWiki.PlantUMLMacroGClass is the groovy class implementing the embedded 
  PlantUML server. It is only used by the macro if the configuration is 
  undefined. The PlantUML jar is attached to this page.
