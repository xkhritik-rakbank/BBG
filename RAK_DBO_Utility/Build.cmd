@REM Copyright (c) 2004 NEWGEN All Rights Reserved.

@REM *********************JAR BUILDING***************************************************************************
@REM Modify these variables to match your environment
	cls
	set JAVA_HOME="C:\Program Files\Java\jdk1.8.0_152"
	set MYCLASSPATH=bin
	set JARPATH=..
@REM ************************************************************************************************

 	cd %MYCLASSPATH%

@REM mqsocketserver jar
    %JAVA_HOME%\bin\jar -cvfm %JARPATH%\rak_bpm_utility2.jar ..\MANIFEST.MF com\newgen\common\*.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\rak_bpm_utility2.jar com\newgen\ODDD\ODDocDownload\*.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\rak_bpm_utility2.jar com\newgen\DormancyChanges\*.class
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\rak_bpm_utility2.jar com\newgen\main\*.class
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\rak_bpm_utility2.jar ..\src\com\newgen\common\*.java
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\rak_bpm_utility2.jar ..\src\com\newgen\ODDD\ODDocDownload\*.java
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\rak_bpm_utility2.jar ..\src\com\newgen\DormancyChanges\*.java
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\rak_bpm_utility2.jar ..\src\com\newgen\main\*.java
	pause
@REM ************************************************************************************************