<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" version="1.0">
  <xsl:output method="html"/>
  
  <xsl:template match="jdf:Queue">
    <html>
      <head> </head>
      <body>
        <table>
          <tr>
            <th align="left" width="210px">QueueEntryID</th>
            <th align="left">Status</th>
          </tr>
          <xsl:apply-templates/>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="jdf:QueueEntry">
	<tr>
	  <td>
		<xsl:value-of select = "@QueueEntryID" />  
      </td>
      <td>
	    <xsl:value-of select = "@Status" />  
      </td>
	</tr>
  </xsl:template>
  

</xsl:stylesheet>