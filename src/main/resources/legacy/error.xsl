<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:strip-space elements="*"/>
  <xsl:output method="html"/>

  <xsl:template match="/BambiError">
    <xsl:variable name="context" select="@Context"/>
    <html>
      <!-- This is not a JSP error page, just a simple means of displaying Bambi errors -->
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <link rel="stylesheet" type="text/css">
          <xsl:attribute name="href"><xsl:value-of select="$context"/>/css/styles_pc.css</xsl:attribute>
        </link>
        <link rel="icon" type="image/x-icon">
          <xsl:attribute name="href"><xsl:value-of select="$context"/>/favicon.ico</xsl:attribute>
        </link>
        <title>Bambi - Error</title>
      </head>
      <body>
        <img height="70" alt="logo">
          <xsl:attribute name="src"><xsl:value-of select="@Context"/>/logo.gif</xsl:attribute>
        </img>
       <h1>Bambi - Error</h1>
        <a>
          <xsl:attribute name="href"><xsl:value-of select="@Context"/></xsl:attribute>
          Back to device root
        </a>
        <hr/>
        <h2>Oops! Bambi is unable to process your request...</h2>
        <p>
          <b>Error Origin: </b>
          <xsl:value-of select="@errorOrigin"/>
          <br/>
          <b>Query URL: </b>
          <xsl:value-of select="@URL"/>
          <br/>
          <b>Error Message: </b>
          <xsl:value-of select="@errorMsg"/>
          <br/>
          <xsl:if test="@errorDetails">
            <b>Details: </b>
            <br/>
            <code>
              <xsl:value-of select="@errorDetails"/>
            </code>
          </xsl:if>
        </p>
        <hr/>
       </body>
    </html>
  </xsl:template>
</xsl:stylesheet>