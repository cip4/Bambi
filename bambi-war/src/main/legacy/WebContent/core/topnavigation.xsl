<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright 2009-2014 CIP4 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:template name="topnavigation">
		<img height="70" alt="logo">
			<xsl:attribute name="src"><xsl:value-of select="/@Context" />/logo.gif</xsl:attribute>
		</img>
	</xsl:template>
</xsl:stylesheet>