<!--  Copyright 2009 CIP4 -->




<!-- ********************************************* -->
<!--This is the contextPath/showQueue/deviceID Page-->
<!-- ********************************************* -->





<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi">
  
  <xsl:param name="refresh"/>
  <xsl:output method="html"/>
  <xsl:template match="jdf:Queue">
  <xsl:variable name="context" select="@Context"/>
  
  
    <html>
      <head>
      
      <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
      <!--[if lt IE 9]>
		<script src="http://css3-mediaqueries-js.googlecode.com/svn/trunk/css3-mediaqueries.js"></script>
	  <![endif]-->
      
      	 <!--<xsl:if test="@refresh='true'">-->
      
      	<script type="text/javascript">
			<xsl:attribute name="src">
				<xsl:value-of select="$context" />/js/refresh.js
			</xsl:attribute>		
		</script>
      
      <!--  
      <meta http-equiv="refresh">
      <xsl:attribute name="content">15; URL=<xsl:value-of
							select="$context" />/showDevice/<xsl:value-of
							select="$deviceID" />?refresh=true</xsl:attribute>
      </meta>
    </xsl:if>-->

    <!-- Google Web Font -->
    <link href='http://fonts.googleapis.com/css?family=Roboto+Condensed:400,700|Roboto:400,500' rel='stylesheet' type='text/css' />
    <!-- Stylesheet -->
    <link href="css/styles_pc.css" rel="stylesheet" type="text/css" />
    <link rel="stylesheet" type="text/css">
    <!--<link href="css/styles_pc.css" rel="stylesheet" type="text/css" />-->
    
    <xsl:attribute name="href"><xsl:value-of
						select="$context" />/css/styles_pc.css</xsl:attribute>
    </link>
    
    
        <xsl:if test="@refresh">
          <meta http-equiv="refresh">
            <xsl:attribute name="content">15; URL=<xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/>?refresh=true</xsl:attribute>
          </meta>
        </xsl:if>
      </head>
      <body>
        <div class="column-grey">
        <div class="headline-wrapper">
        
        
        <h2>
          <xsl:value-of select="@DescriptiveName"/>
        </h2>
        </div>

         
         <div class="box noheight">
         <h3>Queue Entry Status</h3>
         
         
         <div class="subrow">
         <p>All</p>
              <xsl:value-of select="@TotalQueueSize"/>
         </div> <!-- subrow -->
         
         
         <div class="subrow">
            <p>Shown</p>
              <div class="bold">
              <xsl:value-of select="count(jdf:QueueEntry)"/>
              </div>
          </div> <!-- subrow -->  
          <xsl:if test="count(jdf:QueueEntry[@Status='Waiting'])>0">
         
         
          <div class="subrow">
            <p>Waiting</p>
                <div class="bold">
              <xsl:value-of select="count(jdf:QueueEntry[@Status='Waiting'])"/>
              </div>
		  </div> <!-- subrow -->
         
         
          </xsl:if>
          <xsl:if test="count(jdf:QueueEntry[@Status='Running'])>0">
          <div class="subrow">
            <p>Running</p>
                <div class="bold">
              <xsl:value-of select="count(jdf:QueueEntry[@Status='Running'])"/>
              </div>
          </div> <!-- subrow -->    
          </xsl:if>
         
         
          <xsl:if test="count(jdf:QueueEntry[@Status='Held'])>0">
          <div class="subrow">
           <p>Held</p>
                <div class="bold">
              <xsl:value-of select="count(jdf:QueueEntry[@Status='Held'])"/>
              </div>
          </div> <!-- subrow -->    
          </xsl:if>
         
         
          <xsl:if test="count(jdf:QueueEntry[@Status='Suspended'])>0">
          <div class="subrow">
           <p>Suspended</p>
                <div class="bold">
              <xsl:value-of select="count(jdf:QueueEntry[@Status='Suspended'])"/>
              </div>
          </div> <!-- subrow -->    
          </xsl:if>
        
        
          <xsl:if test="count(jdf:QueueEntry[@Status='Completed'])>0">
          <div class="subrow">  
          <p>Completed</p>
                <div class="bold">
              <xsl:value-of select="count(jdf:QueueEntry[@Status='Completed'])"/>
              </div>
          </div> <!-- subrow -->    
          </xsl:if>
         
         
          <xsl:if test="count(jdf:QueueEntry[@Status='Aborted'])>0">
          <div class="subrow">
            <p>Aborted </p>
                <div class="bold">
              <xsl:value-of select="count(jdf:QueueEntry[@Status='Aborted'])"/>
              </div>
          </div> <!-- subrow -->    
          </xsl:if>
        
          </div>
		
        <!--  global queue buttons -->
        <div class="box noheight">
			<div class="subrow">
              <div class="buttonwrapper">
              


            <div class="reload continually"> 
              
              <!-- Reload continually -->
              <form>
                <xsl:attribute name="action">
                <xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/>
                </xsl:attribute>
                <input id="refreshValue" type="hidden"/>
                <input id="refreshButton" type="submit" class="button" value="Refresh continually"/>
              </form>
            </div>
            <div class="buttonsymbol reload continually">
              <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/refresh.svg</xsl:attribute></img>
            </div>

              <!-- Open Queue Button -->
              <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="open" value="true"/>
                <input type="submit" class="button" value="Open"/>
              </form>
              <div class="buttonsymbol">
            <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/open.svg</xsl:attribute></img>
            </div>
              </div>
              
              
              <!-- Close Queue Button -->
              <div class="buttonwrapper">
              <form>
                <xsl:attribute name="action">
                <xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/>
                </xsl:attribute>
                <input type="hidden" name="close" value="true"/>
                <input type="submit" class="button" value="Close"/>
              </form>
              <div class="buttonsymbol">
            <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/close.svg</xsl:attribute></img>
            </div>
           </div>
           
           
           <!-- Resume Queue Button -->
              <div class="buttonwrapper">
              <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="resume" value="true"/>
                <input type="submit" class="button" value="Resume"/>
              </form>
              <div class="buttonsymbol">
                <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/resume.svg</xsl:attribute></img>
              </div>
            </div>
              
              
              <!-- Hold Queue Button --> 
              <div class="buttonwrapper">
              <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="hold" value="true"/>
                <input type="submit" class="button" value="Hold"/>
              </form>
              <div class="buttonsymbol">
            <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/hold.svg</xsl:attribute></img>
            </div>
              </div>
              
              
              
        
        <xsl:if test="@pos&gt;0">
             
             
             <!-- Show first Frame Button -->
             <div class="buttonwrapper">
              <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="pos" value="0"/>
                <input type="submit" class="button" value="&lt;&lt; first" title="show first frame"/>
               <input type="hidden" name="filter">
                  <xsl:attribute name="value"><xsl:value-of select="@filter"/></xsl:attribute>
                </input>
              </form>
              <div class="buttonsymbol">
                <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/arrowleft.svg</xsl:attribute></img>
                </div>
            </div>
            
            
            <!-- Show previous Frame Button -->
              <div class="buttonwrapper">
              <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="pos" >
                 <xsl:attribute name="value"><xsl:value-of select="number(@pos)-1"/></xsl:attribute>
                </input>
                <input type="submit" class="button" value="&lt; previous" title="show previous frame"/>
                <input type="hidden" name="Filter">
                  <xsl:attribute name="value"><xsl:value-of select="@filter"/></xsl:attribute>
                </input>
             </form>
              <div class="buttonsymbol">
                <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/arrowleft.svg</xsl:attribute></img>
                </div>
              </div>
              
            
        </xsl:if>
      <xsl:if test="not(@pos&gt;0)">
            
        </xsl:if>
 			
            
            <!-- Refresh Button -->
             <div class="buttonwrapper">
              <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="submit" class="button" value="Refresh"/>
                <input type="hidden" name="pos" >
                 <xsl:attribute name="value"><xsl:value-of select="number(@pos)"/></xsl:attribute>
                </input>
               <input type="hidden" name="filter">
                  <xsl:attribute name="value"><xsl:value-of select="@filter"/></xsl:attribute>
                </input>
             </form>
              <div class="buttonsymbol">
                <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/refresh.svg</xsl:attribute></img>
                </div>
            </div>
            
            
            <!-- Flush Button -->
              <div class="buttonwrapper">
             <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="hidden" name="flush" value="true"/>
                <input type="submit" class="button" value="Flush"/>
              </form>
              <div class="buttonsymbol">
                <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/flush.svg</xsl:attribute></img>
                </div>
              </div>
              
              </div> <!-- subrow -->
              
       
       
       
        <xsl:if test="@hasNext">
            <!-- Show next frame Button -->
            <div class="buttonwrapper">
              <form>
                <xsl:attribute name="action">
                <xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/>
                </xsl:attribute>
                <input type="hidden" name="pos" >
                 <xsl:attribute name="value"><xsl:value-of select="number(@pos)+1"/></xsl:attribute>
                </input>
                <input type="submit" class="button" value="next &gt;" title="show next frame"/>
              </form>
              <div class="buttonsymbol">
            <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/arrowright.svg</xsl:attribute></img>
            </div>
            </div>
            
            
            <!-- Show last frame Button -->
              <div class="buttonwrapper">
              <form>
                <xsl:attribute name="action">
                <xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/>
                </xsl:attribute>
                <input type="hidden" name="pos" value="-1"/>
                <input type="submit" class="button" value="last &gt;&gt;" title="show last frame"/>
                <input type="hidden" name="filter">
                  <xsl:attribute name="value"><xsl:value-of select="@filter"/></xsl:attribute>
                </input>
              </form>
              <div class="buttonsymbol">
            <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/arrowright.svg</xsl:attribute></img>
            </div>
            </div>
              
         </xsl:if>
       <xsl:if test="not(@hasNext)">
            
        </xsl:if>
            <div class="subrow">
              
              <!-- Filtering -->
              <form>
                <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
                <input type="text" name="filter" class="form-white">
                  <xsl:attribute name="value"><xsl:value-of select="@filter"/></xsl:attribute>
                </input>
               
               <div class="buttonwrapper"> <input type="submit" class="button" value="Filter"/> 
              <div class="buttonsymbol">
            <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/filter.svg</xsl:attribute></img>
            </div> </div></form>
              
            </div>
              
          </div>
        
        <!--<div class="subrow">
        <em>Incoming Entries</em><br/>
        <em>Outgoing Entries</em><br/>
        <em>refresh Queue</em><br/>
        <em>Filter Queue</em>
        <em>Danger flushes entire Queue!</em><br/></div>-->
        
        <div class="column-white column-5">
        <div class="headline-wrapper">
        <h2>Queue Entry Details</h2>
        </div>
        
        <!--  queueentry table description  -->
        <!-- <table cellspacing="1" border="1"> -->	
         <!-- <tr bgcolor="#bbbbbb">
          <th align="center">#</th>
             <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'QueueEntryID'"/>
              <xsl:with-param name="attName" select="'QueueEntryID'"/>
            </xsl:call-template>
            
            
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'Priority'"/>
              <xsl:with-param name="attName" select="'Priority'"/>
            </xsl:call-template>
            
            
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'Submission Date / Time'"/>
              <xsl:with-param name="attName" select="'SubmissionTime'"/>
            </xsl:call-template>
            
            
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'Start Date / Time'"/>
              <xsl:with-param name="attName" select="'StartTime'"/>
            </xsl:call-template>
            
            
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'End Date / Time'"/>
              <xsl:with-param name="attName" select="'EndTime'"/>
            </xsl:call-template>
            
            
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'Description'"/>
              <xsl:with-param name="attName" select="'DescriptiveName'"/>
            </xsl:call-template>
            
            
           <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'Job ID'"/>
              <xsl:with-param name="attName" select="'JobID'"/>
            </xsl:call-template>
            
            
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'JobPartID'"/>
              <xsl:with-param name="attName" select="'JobPartID'"/>
            </xsl:call-template>
            
            
            <xsl:if test="@bambi:SlaveURL='true'">
              <xsl:call-template name="qeHeader">
                <xsl:with-param name="display" select="'Slave QueueEntryID'"/>
                <xsl:with-param name="attName" select="'bambi:SlaveQueueEntryID'"/>
              </xsl:call-template>
              
              
            </xsl:if>
            <th align="center">Device</th>
            <xsl:call-template name="qeHeader">
              <xsl:with-param name="display" select="'Status'"/>
              <xsl:with-param name="attName" select="''"/>
            </xsl:call-template>
          </tr>-->
          
          <xsl:apply-templates/>
          
       <!-- </table>-->
		
        </div>
   <!--     <form>
          <xsl:attribute name="action"><xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID"/></xsl:attribute>
          <input type="submit" class="button" value="refresh queue"/>
               <input type="hidden" name="pos" >
                 <xsl:attribute name="value"><xsl:value-of select="number(@pos)-1"/></xsl:attribute>
                </input>
         </form>-->
         </div>
      </body>
    </html>
  </xsl:template>




<!-- ********************************************* QueueHeader **************************************************** -->
  <xsl:template name="qeHeader">
    <xsl:param name="display"/>
    <xsl:param name="attName"/>
    
    <div align="center">
      <a>
        <xsl:attribute name="href">
                 <xsl:value-of select="@Context"/>/showQueue/<xsl:value-of select="@DeviceID"/>?SortBy=<xsl:value-of select="$attName"/><xsl:if test="@filter">&amp;filter=<xsl:value-of select="@filter"/></xsl:if>
        </xsl:attribute>
        <xsl:value-of select="$display"/>
      </a>
    </div>
  </xsl:template>
  
  
  
  
  
  
  <!-- ********************************************** QueueEntry template ********************************************** -->
  
  <xsl:template match="jdf:QueueEntry">
    <xsl:variable name="context" select="../@Context"/>
    <div>
      <xsl:if test="@Status='Running'">
        <xsl:attribute name="class">queueEntry box noheight running</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Waiting'">
        <xsl:attribute name="class">queueEntry box noheight waiting</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Suspended'">
        <xsl:attribute name="class">queueEntry box noheight suspended</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Held'">
        <xsl:attribute name="class">queueEntry box noheight held</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Aborted'">
        <xsl:attribute name="class">queueEntry box noheight aborted</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Completed'">
        <xsl:attribute name="class">queueEntry box noheight completed</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='PendingReturn'">
        <xsl:attribute name="class">queueEntry box noheight pendingreturn</xsl:attribute>
      </xsl:if>
  <div class="queueEntryDetails previewwrapper">
  
  
  
  <!-- Entry Number -->
  <div class="Number float-left">
  <form>
   <xsl:variable name ="c1" select="500*number(../@pos)"/>
   
   <xsl:variable name ="c2"> <xsl:number count="jdf:QueueEntry" /></xsl:variable>
   
   <xsl:value-of select="$c1 + $c2"/>
    
    
    <!--  submission button for pulling jobs -->
    <xsl:if test="../@Pull='true'">
      <xsl:if test="@Status='Waiting'">
        <xsl:if test="count(../jdf:QueueEntry[@Status='Running'])=0">
          <form>
            <xsl:attribute name="action"><xsl:value-of select="$context"/>/modifyQE/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
            <input type="hidden" name="qeID">
              <xsl:attribute name="value"><xsl:value-of select="@QueueEntryID"/></xsl:attribute>
            </input>
            <input type="hidden" name="submit" value="true"/>
            <input type="submit" class="button" value="submit"/>
          </form>
        </xsl:if>
      </xsl:if>
    </xsl:if>
  </form>
  </div>
  
  
  <!-- Name -->
     <div>
        <form>
            <div class="DescriptiveName">
            <xsl:value-of select="@DescriptiveName"/>
            </div>
        </form>
     </div>
        
        
        <!-- ID -->
        <em class="smaller">QueueEntry ID:</em><br/> 
        <em>
            <a>
              <xsl:attribute name="href"><xsl:value-of select="$context"/>/showJDF/<xsl:value-of select="../@DeviceID"/>?qeID=<xsl:value-of
                select="@QueueEntryID"/></xsl:attribute>
              <xsl:value-of select="@QueueEntryID"/>
            </a>
        </em>
      
      
      
      <!-- Status Bar -->
      <div>
      <xsl:if test="@Status='Running'">
        <xsl:attribute name="class">status-bar Running</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Waiting'">
        <xsl:attribute name="class">status-bar Waiting</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Suspended'">
        <xsl:attribute name="class">status-bar Suspended</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Held'">
        <xsl:attribute name="class">status-bar Held</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Aborted'">
        <xsl:attribute name="class">status-bar Aborted</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='Completed'">
        <xsl:attribute name="class">status-bar Completed</xsl:attribute>
      </xsl:if>
      <xsl:if test="@Status='PendingReturn'">
        <xsl:attribute name="class">status-bar PendingReturn</xsl:attribute>
      </xsl:if>
      </div>
      
      </div>
      
      <!-- queueEntryDetails previewwrapper -->
      <!-- <form> -->
      <input type="radio" class="selector open">
      <xsl:attribute name="name">
      queueEntryDetails-<xsl:value-of select="@QueueEntryID"></xsl:value-of>
      </xsl:attribute>
      <xsl:attribute name="id">
      open-<xsl:value-of select="@QueueEntryID"></xsl:value-of>
      </xsl:attribute>
      </input>
      <label class="queueEntryDetails open-label">
      <xsl:attribute name="for">
      open-<xsl:value-of select="@QueueEntryID"></xsl:value-of>
      </xsl:attribute>
      
      
      <div class="open">
      
      
      <img class="center verticalopen">
      <xsl:attribute name="src"><xsl:value-of select="$context" />/images/arrowdown.svg</xsl:attribute>
      </img>
      
      
      </div>
      </label>
      
      
      
      <!-- Details of the queue Entry -->
      
        <div class="queueEntryDetailsWrapper">
         
          <div class="subrow">
          <div class="JobID">
          <em class="smaller">Job ID: </em><br/>
            <em>
            <xsl:value-of select="@JobID"/>
            </em>
          </div>
          
          <div class="JobPartID">
          <em class="smaller">JobPart ID: </em><br/>
            <em>
            <xsl:value-of select="@JobPartID"/>
            </em>
          </div>
          </div>
          
          <div class="subrow">
              <div class="Priority">
               <em class="smaller">Priority: </em><br/>
               <em><xsl:value-of select="@Priority"/></em>
              </div>
          </div>
          
          
          <div class="subrow">
              <div class="SubmissionTime">
                   <em class="smaller">Submission: </em><br/>
                   <em>
                    <xsl:call-template name="dateTime">
                      <xsl:with-param name="val" select="@SubmissionTime"/>
                    </xsl:call-template>
                   </em>
               </div>     
           </div>
           
          <div class="subrow">
              <div class="StartTime">
                  <em class="smaller">Start: </em><br/>
                  <em>
                    <xsl:call-template name="dateTime">
                      <xsl:with-param name="val" select="@StartTime"/>
                    </xsl:call-template>
                  </em>
              </div>      
          </div>
          
          <div class="subrow">
              <div class="EndTime">
                <em class="smaller"> End: </em><br/>
                  <em>
                    <xsl:call-template name="dateTime">
                      <xsl:with-param name="val" select="@EndTime"/>
                    </xsl:call-template>
                  </em>
                    
              </div>
          </div>
          
    
          <xsl:if test="../@bambi:SlaveURL='true'">
            <div class="subrow">
                SlaveQueueEntry ID: 
                <div class="SlaveQueueEntryID">
                  <xsl:value-of select="@bambi:SlaveQueueEntryID"/>
            	</div>
            </div>
          </xsl:if>
          
          
          <!--<div class="DeviceID">
         	  <div class="subrow">
                  <em>Device:</em>
                  <xsl:value-of select="@DeviceID"/>
          	  </div>
          </div>-->
          
          
          <div class="subrow">
              <div class="modify">
                <!-- calls the optionList -->
                <form>
                  <xsl:attribute name="action"><xsl:value-of select="$context"/>/modifyQE/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
                  <xsl:apply-templates/>
                  <input type="hidden" name="qeID">
                    <xsl:attribute name="value"><xsl:value-of select="@QueueEntryID"/></xsl:attribute>
                  </input>
                  <input type="submit" class="button" value="modify entry"/>
                </form>
                <div class="clear"></div>
              </div>
          </div>
    
          
        </div> <!-- queueEntryDetails detailwrapper -->
        
      
      
      <!-- Close-Button -->
      <input type="radio" class="selector close">
      <xsl:attribute name="name">
      queueEntryDetails-<xsl:value-of select="@QueueEntryID"></xsl:value-of>
      </xsl:attribute>
      <xsl:attribute name="id">
      close-<xsl:value-of select="@QueueEntryID"></xsl:value-of>
      </xsl:attribute>
      </input>
      <label class="queueEntryDetails close-label">
      <xsl:attribute name="for">
      close-<xsl:value-of select="@QueueEntryID"></xsl:value-of>
      </xsl:attribute>
      
      
      
      <div class="close">
      
      
      <img class="center verticalclose">
      <xsl:attribute name="src"><xsl:value-of select="$context" />/images/close.svg</xsl:attribute>
      </img>
      
      
      </div>
      </label>
      <!-- </form> -->

      <div class="clear"></div>
    </div>
  </xsl:template>
  <xsl:include href="optionlist.xsl"/>
  <xsl:include href="StandardXML.xsl"/>
</xsl:stylesheet>