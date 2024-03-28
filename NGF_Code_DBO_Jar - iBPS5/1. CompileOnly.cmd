@REM Copyright (c) 2004 NEWGEN All Rights Reserved.

@REM ************************************************************************************************
@REM Modify these variables to match your environment
	cls
	set JAVA_HOME="C:\Program Files\Java\jdk1.8.0_161"
	set JTS_LIBPATH=lib
	set MYCLASSPATH=bin
	set LIBCLASSPATH=%JTS_LIBPATH%\iforms.jar;%JTS_LIBPATH%\json-simple-1.1.1.jar;%JTS_LIBPATH%\log4j-1.2.14.jar;%JTS_LIBPATH%\wfdesktop.jar;%JTS_LIBPATH%\xmlworker-5.5.13.2.jar;%JTS_LIBPATH%\itextpdf-5.5.13.2.jar;%JTS_LIBPATH%\commons-io-2.8.0.jar;%JTS_LIBPATH%\nsms.jar;%JTS_LIBPATH%\ISPack.jar;%JTS_LIBPATH%\jdts.jar;%JTS_LIBPATH%\ngejbcallbroker.jar;
@REM ************************************************************************************************

@REM ************************************************************************************************
@REM Compile SockectClient
	
	%JAVA_HOME%\bin\javac -d %MYCLASSPATH% -classpath %LIBCLASSPATH%;%MYCLASSPATH% src\com\newgen\iforms\user\*.java
	pause
@REM ************************************************************************************************


