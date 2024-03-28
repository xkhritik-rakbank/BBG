@REM Modify these variables to match your environment
	cls
	set JAVA_HOME="C:\Program Files\Java\jdk1.8.0_161"
	set MYCLASSPATH=bin
	set JARPATH=..
@REM ************************************************************************************************

 	cd %MYCLASSPATH%

@REM mqsocketserver jar
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\iRBL.jar com\newgen\iforms\user\EventHandler.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\iRBL.jar com\newgen\iforms\user\iRBL_Click.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\iRBL.jar com\newgen\iforms\user\iRBL_FormLoad.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\iRBL.jar com\newgen\iforms\user\iRBL_IntroDone.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\iRBL.jar com\newgen\iforms\user\iRBLCommon.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\iRBL.jar com\newgen\iforms\user\iRBLIntegration.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\iRBL.jar com\newgen\iforms\user\iRBL_GeneratePDF.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\iRBL.jar com\newgen\iforms\user\iRBL_DecTechCall.class
	pause
@REM ************************************************************************************************


