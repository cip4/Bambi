<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1">
  <xsl:strip-space elements="*"/>
  <xsl:output method="html"/>
  <xsl:template match="/XMLDevice">
    <html>
      <xsl:variable name="deviceID" select="@DeviceID"/>
      <xsl:variable name="deviceType" select="@DeviceType"/>
      <xsl:variable name="deviceURL" select="@DeviceURL"/>
      <xsl:variable name="deviceStatus" select="@DeviceStatus"/>
      <xsl:variable name="context" select="@Context"/>
      <xsl:variable name="modify" select="@modify"/>
      <head>
        <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href"><xsl:value-of select="$context"/>/css/styles_pc.css</xsl:attribute>
        </link>
        <link rel="icon" type="image/x-icon">
          <xsl:attribute name="href"><xsl:value-of select="$context"/>/favicon.ico</xsl:attribute>
        </link>
        <title>
          <xsl:value-of select="$deviceType"/>
          - Operator Login for device:
          <xsl:value-of select="$deviceID"/>
        </title>
      </head>

      <!-- Body only  -->
      <body class="popupcontent">
        
        <!-- Header -->
        <div class="headline-wrapper">
            <h2>
              <xsl:value-of select="$deviceID"/> Login
            </h2>
            <xsl:apply-templates select="KnownEmployees"/>
        </div>
        
        <!-- Logged in -->    
        <!--<xsl:if test="jdf:Employee">-->
        <div class="box noheight loggedin clear">
            <div class="headline-wrapper">
            	<h2 class="box-headline">Logged in</h2>
            </div>
                <xsl:for-each select="jdf:Employee">
                  <xsl:call-template name="modifyEmployee"/>
                </xsl:for-each>
            <div class="clear"></div>
        </div><!-- end of Loggedin -->
        <!--</xsl:if>-->

      </body>
    </html>
  </xsl:template>
  
  
  <!--  login Employee -->
  <xsl:template match="KnownEmployees">
    	
     <div class="login">
        <form>
            <select name="PersonalID" class="select-box-white clear">
              <xsl:for-each select="jdf:Employee">
                <xsl:call-template name="loginEmployee"/>
              </xsl:for-each>
            </select>
		
            <input type="submit" value="login" title="login operator" class="button clear"/>
            <input type="hidden" name="inout" value="login"/>
    	</form>
            <div class="clear"></div>
    </div>
  </xsl:template>

  <!--  Employee Dropdown Elements -->
  <xsl:template name="loginEmployee">
    <option>
      <xsl:value-of select="@ProductID"/>-<xsl:value-of select="jdf:Person/@DescriptiveName"/>
    </option>
  </xsl:template>

  <!--  modifiable Employee -->
  <xsl:template name="modifyEmployee">
    <xsl:variable name="context" select="/XMLDevice/@Context"/>
    <xsl:variable name="deviceID" select="/XMLDevice/@DeviceID"/>
		<div class="row">
            <form>
              <xsl:attribute name="action"><xsl:value-of select="$context"/>/login/<xsl:value-of select="$deviceID"/></xsl:attribute>
              <input type="hidden" name="PersonalID">
                <xsl:attribute name="value"><xsl:value-of select="@ProductID"/></xsl:attribute>
              </input>
              <input type="hidden" name="inout" value="logout"/>
              <input type="submit" title="log off operator">
                <xsl:attribute name="value">log off <xsl:value-of select="@ProductID"/></xsl:attribute>
              </input>
            </form>
            <div class="clear"></div>
		</div><!-- end of row -->
        <div class="row">
        	<h2>Name</h2>
        	<xsl:value-of select="jdf:Person/@DescriptiveName"/>
            <div class="clear"></div>
		</div><!-- end of row -->
        <div class="row">
        	<h2>Roles</h2>
        	<xsl:value-of select="@Roles"/>
            <div class="clear"></div>
		</div><!-- end of row -->
  </xsl:template>
</xsl:stylesheet>