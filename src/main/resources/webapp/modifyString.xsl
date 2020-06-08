<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<!-- simple dispay / form swapper -->
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml"
	xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template name="modifyString">
		<xsl:param name="attLabel" />
		<xsl:param name="attName" />
		<xsl:param name="attVal" />
		<xsl:param name="modify" />
		<xsl:param name="desc" select="''" />
		
				<div class="box">
                
                <h3><xsl:value-of select="$attLabel" /></h3>

				<xsl:choose>
					
                    <xsl:when test="$modify='true'">
						
                        <input type="text" maxLength="255">
							<xsl:attribute name="value"><xsl:value-of
								select="$attVal" /></xsl:attribute>
							<xsl:attribute name="name"><xsl:value-of
								select="$attName" /></xsl:attribute>
							
                            <xsl:if test="$attName='Dump'">
								<xsl:attribute name="type">checkbox</xsl:attribute>
								<xsl:if test="$attVal='true'">
									<xsl:attribute name="checked">true</xsl:attribute>
								</xsl:if>
							</xsl:if>
						</input>
						
                        <xsl:if test="$attName='Dump'">
							<input type="hidden" name="UpdateDump" value="true" />
						</xsl:if>
					</xsl:when>
                    
                    
					<xsl:when test="contains($attName,'URL')">
						<a>
							<xsl:attribute name="href">
                            	<xsl:value-of select="$attVal" />
                            </xsl:attribute>
							<em><xsl:value-of select="$attVal" /></em>
						</a>
					</xsl:when>
                    
                    
					<xsl:otherwise>
						<em><xsl:value-of select="$attVal" /></em>
					</xsl:otherwise>
                    
                    
				</xsl:choose>

				<xsl:if test="$desc">
					<br/><em class="smaller"><xsl:value-of select="$desc" /></em>
				</xsl:if>
			
            </div>
	</xsl:template>

</xsl:stylesheet>