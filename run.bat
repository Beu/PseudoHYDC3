rem
rem You should modify JAVA_HOME variable
rem
rem set JAVA_HOME=c:\progra~1\java\jre
rem set JAVA_HOME=c:\progra~1\java\jdk1.6.0_11

rem
rem You can specify the font with hydc3.fontName (usually, traditional Chinese font):
rem -Dhydc3.fontName=PMingLiU
rem -Dhydc3.fontName=DFKai-SB
rem -Dhydc3.fontName=SimSun
rem and you can specify the look&feel with swing.defaultlaf:
rem -Dswing.defaultlaf=com.sun.java.swing.plaf.motif.MotifLookANdFeel
rem -Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel
rem -Dswing.defaultlaf=com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel

%JAVA_HOME%\bin\java -cp .\bin;.\lib\derby.jar -version:1.6+ -Xmx256m -Dhydc3.fontName=PMingLiU PseudoHYDC3
