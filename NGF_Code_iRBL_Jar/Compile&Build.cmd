@REM Copyright (c) 2004 NEWGEN All Rights Reserved.

@REM ************************************************************************************************
@REM Modify these variables to match your environment
	cls
	set JAVA_HOME="C:\Program Files\Java\jdk1.8.0_161"
	set JTS_LIBPATH=lib
	set MYCLASSPATH=bin
	set LIBCLASSPATH=%JTS_LIBPATH%\iforms.jar;%JTS_LIBPATH%\json-simple-1.1.1.jar;%JTS_LIBPATH%\log4j-1.2.14.jar;%JTS_LIBPATH%\wfdesktop.jar
@REM ************************************************************************************************

@REM ************************************************************************************************
@REM Compile SockectClient
	
	%JAVA_HOME%\bin\javac -d %MYCLASSPATH% -classpath %LIBCLASSPATH%;%MYCLASSPATH% src\com\newgen\iforms\user\*.java
	pause
@REM ************************************************************************************************


@REM Copyright (c) 2004 NEWGEN All Rights Reserved.

@REM *********************JAR BUILDING***************************************************************************
@REM Modify these variables to match your environment
	cls
	set JAVA_HOME="C:\Program Files\Java\jdk1.8.0_161"
	set MYCLASSPATH=bin
	set JARPATH=..
@REM ************************************************************************************************

 	cd %MYCLASSPATH%

@REM mqsocketserver jar
    %JAVA_HOME%\bin\jar -cvf %JARPATH%\iRBL.jar com\newgen\iforms\user\*.class    
	pause
@REM ************************************************************************************************


