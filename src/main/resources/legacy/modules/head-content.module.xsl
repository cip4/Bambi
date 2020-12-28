<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi">
    <xsl:template name="head-content">
        <xsl:variable name="context" select="@Context" />
        
        <meta charset="UTF-8" />
        <title>CIP4 Bambi</title>

        <!-- bootstrap 4 -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css"></link>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"></script>

        <!-- google fonts -->
        <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Merriweather:400,400i,700%7CRoboto:300,300i,400,400i,500,500i,700,700i"></link>

        <!-- bambi style -->
        <link rel="stylesheet">
            <xsl:attribute name="href"><xsl:value-of select="$context" />/legacy/styles/style.css</xsl:attribute>
        </link>
        <link rel="icon" type="image/x-icon">
            <xsl:attribute name="href"><xsl:value-of select="$context" />/legacy/favicon.ico</xsl:attribute>
        </link>
        <script src="{$context}/legacy/styles/style.js"/>
    </xsl:template>
</xsl:stylesheet>