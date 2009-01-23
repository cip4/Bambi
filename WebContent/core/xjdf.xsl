
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xjdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi">
  <xsl:strip-space elements="*"/>
  <!--  device processor -->
  <xsl:template match="xjdf:XJDF">
    <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <link rel="stylesheet" type="text/css" href="../css/styles_pc.css"/>
        <link rel="icon" href="favicon.ico" type="image/x-icon"/>
        <title>
          JDF Single Node
          <xsl:value-of select="@JobID"/>
          /
          <xsl:value-of select="@JobPartID"/>
        </title>
      </head>
      <body>
        <img src="../logo.gif" height="70" alt="logo"/>
        <a>
          <xsl:attribute name="href">./<xsl:value-of select="@DeviceID"/>?qeID=<xsl:value-of select="@QueueEntryID"/></xsl:attribute>
          Back to List of JDF nodes
        </a>
        -
        <a>
          <xsl:attribute name="href">./<xsl:value-of select="@DeviceID"/>?raw=true&amp;qeID=<xsl:value-of
            select="@QueueEntryID"/>&amp;JobPartID=<xsl:value-of select="@JobPartID"/></xsl:attribute>
          Show Raw XJDF
        </a>
        
        <h1>
          JDF Single Node
          <xsl:value-of select="@JobID"/>
          /
          <xsl:value-of select="@JobPartID"/>
        </h1>
        <xsl:if test="@CommentURL">
          <a>
            <xsl:attribute name="href"><xsl:value-of select="@CommentURL"/></xsl:attribute>
            External Job Description
          </a>
          </xsl:if>

        <hr/>
        <xsl:call-template name="printAttributelines">
          <xsl:with-param name="x1" select="'Context'"/>
          <xsl:with-param name="x2" select="'xsi:type'"/>
          <xsl:with-param name="x3" select="'DeviceID'"/>
         <xsl:with-param name="x4" select="'CommentURL'"/>
        <xsl:with-param name="x5" select="'ID'"/>
        </xsl:call-template>
        <hr/>
        <xsl:apply-templates/>
        <a>
          <xsl:attribute name="href">./<xsl:value-of select="@DeviceID"/>?qeID=<xsl:value-of select="@QueueEntryID"/></xsl:attribute>
          Back to JDF node List
        </a>
      </body>
    </html>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:Comment">
     <table >
      <tr>
        <td >
        <em>
      User Comment:
      <xsl:if test="@Name">
        -
        <xsl:value-of select="@Name"/>
      </xsl:if>
      </em>
        </td>
        <td border="2">
          <xsl:value-of select="."/>
        </td>
      </tr>
    </table>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:GeneralID">
    <table Border="0" cellspacing="0">
      <tr>
        <td nowrap="true">
          <em>
            <xsl:value-of select="@IDUsage"/>
            :
          </em>
        </td>
        <td nowrap="true">
          <xsl:value-of select="@IDValue"/>
        </td>
      </tr>
    </table>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->
  <xsl:template match="xjdf:ParameterSet">
    <xsl:call-template name="set">
      <xsl:with-param name="header" select="'Parameter Set:'"/>
    </xsl:call-template>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:Parameter">
    <xsl:call-template name="xjdfResource">
      <xsl:with-param name="header" select="'Parameter'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="xjdf:IntentSet">
    <xsl:call-template name="set">
      <xsl:with-param name="header" select="'Product Intent Set:'"/>
    </xsl:call-template>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:Intent">
    <xsl:call-template name="xjdfResource">
      <xsl:with-param name="header" select="'Product Intent Resource'"/>
    </xsl:call-template>
  </xsl:template>
  <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:ResourceSet">
    <xsl:call-template name="set">
      <xsl:with-param name="header" select="'Resource Set:'"/>
    </xsl:call-template>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:Resource">
    <xsl:call-template name="xjdfResource">
      <xsl:with-param name="header" select="'Resource'"/>
    </xsl:call-template>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:Part">
    <xsl:call-template name="printAttributes">
      <xsl:with-param name="prefix" select="'Partition:'"/>
    </xsl:call-template>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:MISDetails">
    <xsl:call-template name="printAttributes">
      <xsl:with-param name="prefix" select="'Cost Charging:'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </xsl:template>
  <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:FileSpec">
    <table Border="0" cellspacing="0">
      <tr>

        <td nowrap="true">
          <em>
            File: 
            </em>
        </td>
        <td nowrap="true">
          <a>
            <xsl:attribute name="href"><xsl:value-of select="@URL"/></xsl:attribute>
            <xsl:value-of select="@URL"/>
          </a>
        </td>
        <td>
          <xsl:call-template name="printAttributes">
            <xsl:with-param name="x1" select="'URL'"/>
          </xsl:call-template>
        </td>
      </tr>
    </table>
    <xsl:apply-templates/>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:SeparationSpec">
    <td>
      <xsl:value-of select="@Name"/>
      ,
    </td>
  </xsl:template>
  <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:Dependent">
    <table Border="0" cellspacing="0">
      <tr>
        <td nowrap="true">
          <a>
            <xsl:attribute name="href">./<xsl:value-of select="/*/@DeviceID"/>?qeID=<xsl:value-of select="/*/@QueueEntryID"/>&amp;JobPartID=<xsl:value-of select="@JobPartID"/></xsl:attribute>
          Dependent 
          <xsl:if test="../@Usage='Input'">
          prior
          </xsl:if>
          <xsl:if test="../@Usage='Output'">
          next
          </xsl:if>
          Workstep:
        </a>
      </td>

      <xsl:for-each select="@*">
        <td nowrap="true">
          <xsl:value-of select="name()"/>
        </td>
        <td>
          =
          <xsl:value-of select="."/>
        </td>
        <td>
        </td>
      </xsl:for-each>
    </tr>
  </table>
 
</xsl:template>

  <!--   ///////////////////////////////////////////////// -->
  <xsl:template match="xjdf:ColorantOrder">
    <xsl:call-template name="separationList"/>
  </xsl:template>
 <!--   ///////////////////////////////////////////////// -->
  <xsl:template match="xjdf:ColorsUsed">
    <xsl:call-template name="separationList"/>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->
  <xsl:template match="xjdf:ProductList">
    <h3>List Of Products</h3>
    <xsl:apply-templates/>
  </xsl:template>
  <!--   ///////////////////////////////////////////////// -->
  <xsl:template match="xjdf:AuditPool">
    <h2>AuditPool</h2>
    <xsl:apply-templates/>
    <hr/>
  </xsl:template>
 <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:Created">
    <xsl:call-template name="audit"/>
  </xsl:template>
  <xsl:template match="xjdf:Modified">
    <xsl:call-template name="audit"/>
  </xsl:template>
  <xsl:template match="xjdf:PhaseTime">
    <xsl:call-template name="audit"/>
  </xsl:template>
  <xsl:template match="xjdf:ResourceAudit">
    <xsl:call-template name="audit"/>
  </xsl:template>
 <xsl:template match="xjdf:Notification">
    <xsl:call-template name="audit">
      <xsl:with-param name="header" select="@Class"/>  
      <xsl:with-param name="xx1" select="'Class'"/>  
    </xsl:call-template>
  </xsl:template>
 <xsl:template match="xjdf:ProcessRun">
    <xsl:call-template name="audit"/>
  </xsl:template>
  
 <!--   ///////////////////////////////////////////////// -->
 <xsl:template name="audit">
 <xsl:param name="header"></xsl:param>
 <xsl:param name="xx1"></xsl:param>
    <h3><xsl:value-of select="name()"/> Audit: <xsl:value-of select="@TimeStamp"/> 
    <xsl:if test="$header">
    - <xsl:value-of select="$header"/> 
    </xsl:if>
    </h3>
   <xsl:call-template name="printAttributelines">
      <xsl:with-param name="x1" select="'TimeStamp'"/>  
     <xsl:with-param name="x2" select="'AgentName'"/>  
     <xsl:with-param name="x3" select="'AgentVersion'"/>  
     <xsl:with-param name="x4" select="'ID'"/>  
     <xsl:with-param name="x5" select="'Author'"/>  
     <xsl:with-param name="x6" select="'ref'"/>  
     <xsl:with-param name="x7" select="$xx1"/>  
    </xsl:call-template>
    <xsl:apply-templates/>
  </xsl:template>
  <!--   ///////////////////////////////////////////////// -->
  <xsl:template match="xjdf:Product">
    <h4>
    <a>
		    <xsl:attribute name="name"><xsl:value-of select="@ID"/></xsl:attribute>
    </a>
      Product:
     </h4>
    <xsl:call-template name="printAttributelines">
      <!--  <xsl:with-param name="x1" select="'Name'"/>  -->
    </xsl:call-template>
    <xsl:apply-templates/>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->
  <xsl:template match="xjdf:ColorPool">
    <h3>Colors in the Job</h3>
    <xsl:apply-templates/>
    <hr/>
  </xsl:template>
  <!--   ///////////////////////////////////////////////// -->
  <xsl:template match="xjdf:Color">
  <hr />
 <table>
  <tr valign="bottom">
  <td width="400">
     <h4 valign="bottom">
      Color:
      <xsl:value-of select="@Name"/>
    </h4>
    </td>
    <td width="50"><xsl:attribute name="bgcolor"><xsl:value-of select="@HTMLColor"/></xsl:attribute>
    </td>
    </tr>
    </table>
    <xsl:call-template name="printAttributelines">
      <xsl:with-param name="x1" select="'Name'"/>
      <xsl:with-param name="x2" select="'HTMLColor'"/>
    </xsl:call-template>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->
  <xsl:template match="xjdf:MarkObject">
     <xsl:call-template name="placedobject">
      <xsl:with-param name="header" select="'Printer Mark'"/>
    </xsl:call-template>
  </xsl:template>
 <!--   ///////////////////////////////////////////////// -->
  <xsl:template match="xjdf:ContentObject">
     <xsl:call-template name="placedobject">
      <xsl:with-param name="header" select="'Imposed Page'"/>
    </xsl:call-template>
  </xsl:template>
  <!--   ///////////////////////////////////////////////// -->
  <xsl:template name="placedobject">
  <xsl:param name="header"/>
    <h4>
      <xsl:value-of select="$header"/>
      <xsl:if test="@Ord">
      <xsl:text> </xsl:text>
     <xsl:value-of select="@Ord"/>
     </xsl:if>
    </h4>
    <xsl:call-template name="printAttributelines">
      <xsl:with-param name="x1" select="'Ord'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </xsl:template>
  <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:Media">
    <h4>
      Media - <xsl:value-of select="@MediaType"/>
    </h4>
    <xsl:call-template name="printAttributelines">
      <xsl:with-param name="x1" select="'MediaType'"/>
    </xsl:call-template>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:DeviceColorantOrder">
    <xsl:call-template name="separationList"/>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->
  <xsl:template match="xjdf:ColorantParams">
    <xsl:call-template name="separationList"/>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->
  <xsl:template match="xjdf:ColorantAlias">
    <table Border="0" cellspacing="0">
      <tr>
        <td>
          ColorantAlias - ReplacementColorantName =
          <xsl:value-of select="@ReplacementColorantName"/>
          :
        </td>
        <xsl:apply-templates/>
      </tr>
    </table>

  </xsl:template>
 <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:ComChannel">
   <xsl:variable name="prefix">
       <xsl:value-of select="@ChannelType"/>
       =
      <xsl:value-of select="@Locator"/>
    </xsl:variable>
    <xsl:call-template name="printAttributes">
      <xsl:with-param name="prefix" select="$prefix"/>
     <xsl:with-param name="x1" select="'ChannelType'"/>
     <xsl:with-param name="x2" select="'Locator'"/>
    </xsl:call-template>
  </xsl:template>
 <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:NumberSpan">
    <xsl:call-template name="span"/>
  </xsl:template>
 <xsl:template match="xjdf:OptionSpan">
    <xsl:call-template name="span"/>
  </xsl:template>
   <xsl:template match="xjdf:DurationSpan">
    <xsl:call-template name="span"/>
  </xsl:template>
 <xsl:template match="xjdf:NameSpan">
    <xsl:call-template name="span"/>
  </xsl:template>
<xsl:template match="xjdf:ShapeSpan">
    <xsl:call-template name="span"/>
  </xsl:template>
<xsl:template match="xjdf:XYPairSpan">
    <xsl:call-template name="span"/>
  </xsl:template>
<xsl:template match="xjdf:MatrixSpan">
    <xsl:call-template name="span"/>
  </xsl:template>
<xsl:template match="xjdf:StringSpan">
    <xsl:call-template name="span"/>
  </xsl:template>
<xsl:template match="xjdf:TimeSpan">
    <xsl:call-template name="span"/>
  </xsl:template>
 <xsl:template match="xjdf:IntegerSpan">
    <xsl:call-template name="span"/>
  </xsl:template>
  <xsl:template match="xjdf:EnumerationSpan">
    <xsl:call-template name="span"/>
  </xsl:template>
  <xsl:template match="xjdf:IntegerSpan">
    <xsl:call-template name="span"/>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->
<!--   ///////////////////////////////////////////////// -->

  <xsl:template name="span">
    <xsl:param name="header"/>
    <xsl:variable name="prefix">
      <xsl:value-of select="$header"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="@Name"/>:
    </xsl:variable>
     
    <xsl:call-template name="printAttributes">
      <xsl:with-param name="prefix" select="$prefix"/>
      <xsl:with-param name="x1" select="'Name'"/>
    </xsl:call-template>
    <xsl:apply-templates>
      <xsl:with-param name="printme" select="''"/>
    </xsl:apply-templates>
 
 </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <xsl:template name="separationList">
    <xsl:param name="pre" select="''"/>
    <table Border="0" cellspacing="0">
      <tr>
        <td>
          <xsl:if test="$pre">
            <xsl:value-of select="$pre"/>
            /
          </xsl:if>
          <xsl:value-of select="name()"/>
          :
        </td>
        <xsl:apply-templates/>
      </tr>
    </table>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <xsl:template match="xjdf:*">
    <xsl:param name="pre"/>
    <xsl:param name="printme" select="'y'"/>
    <xsl:variable name="pre2">
      <xsl:if test="$printme">
        <xsl:if test="$pre">
          <xsl:value-of select="$pre"/>
          /
        </xsl:if>
        <xsl:value-of select="name()"/>
      </xsl:if>
    </xsl:variable>
    <xsl:call-template name="printAttributelines">
      <xsl:with-param name="prefix" select="$pre2"/>
    </xsl:call-template>
    <xsl:apply-templates>
      <xsl:with-param name="pre" select="$pre2"/>
    </xsl:apply-templates>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <xsl:template name="set">
    <xsl:param name="header"/>
    <h2>
    <a>
    <xsl:attribute name="href"><xsl:value-of select="@ID"/></xsl:attribute>
    </a>
      <xsl:if test="@Usage">
        <xsl:value-of select="@Usage"/>
        <xsl:text> </xsl:text>
      </xsl:if>

      <xsl:value-of select="$header"/>
        <xsl:text> </xsl:text>
      <xsl:value-of select="@Name"/>
      <xsl:if test="@ProcessUsage">
        (
        <xsl:value-of select="@ProcessUsage"/>
        )
      </xsl:if>
      - Parts: <xsl:value-of select="count(xjdf:Parameter) + count(xjdf:Resource)+ count(xjdf:Intent)"/>
    </h2>
    
    <xsl:call-template name="printAttributelines">
      <xsl:with-param name="x1" select="'ProcessUsage'"/>
      <xsl:with-param name="x2" select="'Name'"/>
      <xsl:with-param name="x3" select="'Usage'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
    <hr/>
  </xsl:template>
  
 <!--   ///////////////////////////////////////////////// -->
  
 <xsl:template name="xjdfResource">
    <xsl:param name="header"/>
   <h3><a>
    <xsl:attribute name="name"><xsl:value-of select="@ID"/></xsl:attribute>
    </a>
   <xsl:value-of select="../@Name"/> Status=<xsl:value-of select="@Status"/></h3>
    <xsl:call-template name="printAttributelines">
      <xsl:with-param name="x1" select="'Status'"/>    
    </xsl:call-template>
    <xsl:apply-templates>
      <xsl:with-param name="printme" select="''"/>
    </xsl:apply-templates>
    <hr/>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->
  <xsl:template name="printRefs">
    <xsl:param name="val" select="."/>
    <xsl:param name="n" select="1"/>
       <xsl:if test=". = $val">
    <td nowrap="true">
        <xsl:value-of select="substring-before(name(),'Ref')"/>
    </td>		     
   <td nowrap="true">
        =
    </td>		     
</xsl:if>

    <td nowrap="true" width="80">
      <a>
        <xsl:choose>
          <xsl:when test="string-length(substring-before($val,' '))=0">
            <xsl:attribute name="href">#<xsl:value-of select="$val"/></xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="href">#<xsl:value-of select="substring-before($val,' ')"/></xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:value-of select="substring-before(name(),'Ref')"/><xsl:value-of select="$n"/>
      </a>
    </td>
    <!-- remove string up to blank and recurse with remaining right string -->
    <xsl:if test="string-length(substring-after($val,' ')) != 0">
      <xsl:call-template name="printRefs">
        <xsl:with-param name="val">
          <xsl:value-of select="substring-after($val,' ')"/>
        </xsl:with-param>
       <xsl:with-param name="n">
          <xsl:value-of select="$n + 1"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>

  </xsl:template>
 <!--   ///////////////////////////////////////////////// -->


  <xsl:template name="printAttributelines">
    <xsl:param name="prefix" select="''"/>
    <xsl:param name="x1" select="''"/>
    <xsl:param name="x2" select="''"/>
    <xsl:param name="x3" select="''"/>
    <xsl:param name="x4" select="''"/>
    <xsl:param name="x5" select="''"/>
    <xsl:param name="x6" select="''"/>
    <xsl:param name="x7" select="''"/>
    <xsl:param name="x8" select="''"/>

    <table Border="0" cellspacing="0">
      <xsl:for-each select="@*">
        <xsl:choose>
          <xsl:when test="$x1 = name()"/>
          <xsl:when test="$x2 = name()"/>
          <xsl:when test="$x3 = name()"/>
          <xsl:when test="$x4 = name()"/>
          <xsl:when test="$x5 = name()"/>
          <xsl:when test="$x6 = name()"/>
          <xsl:when test="$x7 = name()"/>
          <xsl:when test="$x8 = name()"/>
           <xsl:when test="'ID' = name()"/>
          <xsl:when test="string-length(name())>3 and string-length(name()) = 3 + string-length(substring-before(name(),'Ref'))">
          <tr valign="top">
          <xsl:call-template name="printRefs"/>
            </tr>
          </xsl:when>
          <xsl:when test="string-length(name())>4 and string-length(name()) = 4 + string-length(substring-before(name(),'Refs'))">
          <tr valign="top">
          <xsl:call-template name="printRefs"/>
            </tr>
          </xsl:when>
           <xsl:otherwise>
            <tr valign="top">
              <td nowrap="true">
                <xsl:if test="normalize-space($prefix) != ''">
                  <xsl:value-of select="$prefix"/>
                  /
                </xsl:if>
                <xsl:value-of select="name()"/>
              </td>
              <td>=</td>
              <td  nowrap="true">
                <xsl:value-of select="."/>
              </td>
            </tr>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </table>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

  <xsl:template name="printAttributes">
    <xsl:param name="prefix" select="''"/>
    <xsl:param name="x1" select="''"/>
    <xsl:param name="x2" select="''"/>
    <xsl:param name="x3" select="''"/>
    <xsl:param name="x4" select="''"/>
    <xsl:param name="x5" select="''"/>
    <xsl:param name="x6" select="''"/>
    <xsl:param name="x7" select="''"/>
    <xsl:param name="x8" select="''"/>
    <table Border="0" cellspacing="0">
      <tr>
        <xsl:if test="normalize-space($prefix) != ''">
          <td nowrap="true">
          <em>
            <xsl:value-of select="$prefix"/>
            </em>
          </td>
        </xsl:if>

        <xsl:for-each select="@*">
          <xsl:choose>
            <xsl:when test="$x1 = name()"/>
            <xsl:when test="$x2 = name()"/>
            <xsl:when test="$x3 = name()"/>
            <xsl:when test="$x4 = name()"/>
            <xsl:when test="$x5 = name()"/>
            <xsl:when test="$x6 = name()"/>
            <xsl:when test="$x7 = name()"/>
            <xsl:when test="$x8 = name()"/>
           <xsl:when test="'ID' = name()"/>
            <xsl:otherwise>
              <td nowrap="true">
                <xsl:value-of select="name()"/>
              </td>
              <td>
                =
                <xsl:value-of select="."/>
              </td>
              <td>
              </td>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </tr>
    </table>
  </xsl:template>

  <!--   ///////////////////////////////////////////////// -->

</xsl:stylesheet>