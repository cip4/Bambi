<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" 
xmlns:bambi="www.cip4.org/Bambi" 
xmlns:xjdf="http://www.CIP4.org/JDFSchema_2_0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  <xsl:strip-space elements="*"/>
  <xsl:output method="html"/>
  <xsl:template match="/SubscriptionList">
    <xsl:variable name="context" select="@Context"/>
    <xsl:variable name="deviceID" select="@DeviceID"/>
    <html>
      <head>
       <!-- Google Web Font -->
    <link href='http://fonts.googleapis.com/css?family=Roboto+Condensed:400,700|Roboto:400,500' rel='stylesheet' type='text/css' />
    <!-- Stylesheet -->
    <link href="css/styles_pc.css" rel="stylesheet" type="text/css" />
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <link rel="stylesheet" type="text/css" href="/webapp/css/styles_pc.css"/>
        <link rel="icon" href="favicon.ico" type="image/x-icon"/>
        
        
        <title>
          <xsl:value-of select="@DeviceID"/>
          - Subscriptions
        </title>
        
        
      </head><!-- end Head -->
      
      
      <!-- Body -->
      <body class="popupcontent">
        
     <!-- START HEADER -->   
        <div class="headline-wrapper">
          <!-- Headline -->
          <h2>
            <xsl:value-of select="@DeviceID"/>
            - Subscriptions
          </h2>
          

          
          <!-- refresh subscription List -->
          <a>
            <xsl:attribute name="href"><xsl:value-of select="@Context"/>/showSubscriptions/<xsl:value-of select="@DeviceID"/>
            </xsl:attribute>
            <div class="buttonsymbol">
            	<img class="buttonsymbolimage">
            		<xsl:attribute name="src"><xsl:value-of select="$context" />/images/refresh.svg</xsl:attribute>
            	</img>
            </div>
          </a>
          
          
          
        </div>
        
        <!-- START CONTENT -->
        
  
  <!-- ######################################## -->
  
        
        <!-- MESSAGE SUBSCRIPTIONS -->
        <xsl:if test="MsgSubscription">
        
        <div class="MsgSubscription box clear noheight">
          <!-- Subscription counter -->
          
           <div class="headline-wrapper clear">
            <h2>
              Subscriptions:
              <xsl:value-of select="count(MsgSubscription)"/>
            </h2>
          </div>
          
            <xsl:apply-templates select="MsgSubscription"/>
            
       	  </div>
        </xsl:if>
        <!-- SUBSCRIPTIONS -->
        

		<!-- MESSAGE SENDER -->
        <xsl:if test="MessageSender">
            <div class="messagesender clear box noheight">
                <!-- Message Sender Headline -->
                <div class="headline-wrapper clear">
                    <h2>Message Sender Channels</h2>
                </div>
                <!-- MESSAGE SENDER CHANNELS -->
                <xsl:apply-templates select="MessageSender"/>
                <div class="clear"></div>
            </div>
        </xsl:if>
        
        <!-- REMOVED CHANNEL -->
        <xsl:if test="RemovedChannel">
            <div class="removedchannel clear box noheight">
              <xsl:apply-templates select="RemovedChannel"/>
            </div>
		</xsl:if>
        <!-- CPU Timer -->
        <div class="cputimers clear box noheight">
        	<div class="headline-wrapper clear">
                    <h2>CPU Timer</h2>
            </div>
        	<xsl:call-template name="cputimer" />
        </div>
		<!-- MESSAGE SENDER MESSAGE -->
        <xsl:if test="MessageSender/Message">
        
		
		<div class="MessageSenderMessage box noheight clear">
            <div class="headline-wrapper clear">
              <h2>Queued Messages</h2>
            </div>
            <xsl:apply-templates select="MessageSender/Message"/>
        </div>

        </xsl:if>

        <!-- PROXY SUBSCRIPTIONS -->
        <xsl:apply-templates select="ProxySubscriptions"/>

      </body>
    </html>
  </xsl:template>
  <!--  end of template SubscriptionList  -->

  
  <!-- ######################################## -->
  
  
  <!-- START MSGSUBSCRIPTION TEMPLATE -->
  <xsl:template match="MsgSubscription">
	<div class="row">
       <div class="preview-wrapper subrow" >
        
        <h3><xsl:value-of select="position()"/> - Channel ID</h3><em>
        <a>
          <xsl:attribute name="href"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/>?DetailID=<xsl:value-of
            select="@ChannelID"/>
                  </xsl:attribute>
          <xsl:value-of select="@ChannelID"/>
        </a></em>
        
        <div>
        	<xsl:choose>
                <xsl:when test="@LastTime=' - '">
                  
                  <xsl:choose>
                    <xsl:when test="@Sent='0'">
                      <xsl:attribute name="class">status-bar red</xsl:attribute>
                    </xsl:when>
                    
                    <xsl:otherwise>
                      <xsl:attribute name="class">status-bar orange</xsl:attribute>
                    </xsl:otherwise>
                  
                  </xsl:choose>
                </xsl:when>
                
                <xsl:otherwise>
                  <xsl:attribute name="class">status-bar green</xsl:attribute>
                </xsl:otherwise>
              </xsl:choose>
        </div>
        
        
      </div><!-- preview-wrapper -->
      
     <div class="subscriptionDetails">
     
     <!-- First row of Subscription Details -->
          <!--<div class="firstRow">-->
          
              <!-- Messages Queued -->
              <div class="subrow half">
                <h3>Messages Queued</h3><em>
                <xsl:value-of select="@Sent"/></em>
              </div>
        
              <!-- Device ID -->
              <div class="subrow half"> 
                <h3>Device ID</h3><em>
                <xsl:value-of select="@DeviceID"/></em>
              </div>
              
              <!-- QueueEntry ID -->
              <div class="subrow">
                <h3>QueueEntry ID</h3><em>
                <xsl:value-of select="@QueueEntryID"/></em>
              </div>
        	  
              <!-- Subscription URL -->
              <div class="subrow">
                <h3>Subscription URL</h3><em>
                <xsl:value-of select="@URL"/></em>
              </div>
          <!--</div>  end firstRow -->
      
      <!-- Second row of Subscription Details -->
          <div class="secondRow clear">
      		  
              <!-- Signal Type -->
              <div class="subrow">
                <h3>Signal Type</h3><em>
                <xsl:value-of select="@Type"/></em>
              </div>
           
              <!-- Repeat Time -->
              <div class="subrow half">
                <h3>Repeat Time</h3><em>
                <xsl:value-of select="@RepeatTime"/></em>
              </div>
        	  
              <!-- Repeat Step -->
              <div class="subrow half"> 
                <h3>Repeat Step</h3><em>
                <xsl:value-of select="@RepeatStep"/></em>
              </div>
        	  
              <!-- Last Time Queued -->
              <div class="subrow">
                <h3>Last time Queued</h3><em>
                <xsl:value-of select="@LastTime"/></em>
              </div>
              
              <!-- Remove Subscription Button -->
              <div class="subrow">
                <h3>Remove Subscription</h3><em>
                <form>
                  <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
                  <input type="hidden" name="StopChannel" value="true"/>
                  <input type="hidden" name="ChannelID">
                    <xsl:attribute name="value"><xsl:value-of select="@ChannelID"/></xsl:attribute>
                  </input>
                  <input type="submit" value="remove" class="button"/>
                </form></em>
              </div>
          </div> <!-- end secondRow -->
      </div><!-- end of subscriptionDetails -->
      <div class="clear"/>
      </div><!-- end of row -->
      
      
      

  </xsl:template>
  <!--  end of template MsgSubscription  -->

  
  
  <!-- ######################################## -->
  
  
  
  <!-- START TEMPLATE MESSAGE SENDER -->
  <xsl:template match="MessageSender">

         <div class="row MessageSender">
         
             <div class="preview-wrapper" >
             
             <!-- Base URL -->
                <div class="subrow">
                    <h3>Base URL</h3>
                    <xsl:value-of select="@URL"/>
                    <!-- Status Bar -->
                    <div class="status-bar">
                        <xsl:choose>
                          <!-- something is waiting -->
                          <xsl:when test="@Size!='0'">
                            <xsl:choose>
                              <xsl:when test="@pause='true'">
                                <xsl:attribute name="class">status-bar orange</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="@Active='true'">
                                <xsl:attribute name="class">status-bar yellow</xsl:attribute>
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:attribute name="class">status-bar red</xsl:attribute>
                              </xsl:otherwise>
                            </xsl:choose>
                          </xsl:when>
                          <xsl:when test="@pause='true'">
                            <xsl:attribute name="class">status-bar orange</xsl:attribute>
                          </xsl:when>
                          <xsl:when test="@Active='true'">
                           <xsl:choose>
                                <xsl:when test="@Problems='true'">
                            <xsl:attribute name="class">status-bar red</xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                            <xsl:attribute name="class">status-bar green</xsl:attribute>
                                </xsl:otherwise>
                              </xsl:choose>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:attribute name="class">status-bar blue</xsl:attribute>
                          </xsl:otherwise>
                        </xsl:choose>
                    </div><!-- status-bar -->
                </div>
             </div><!-- end previewWrapper -->
        
             <div class="subscriptionDetails">
                
                 <div class="firstRow">
                    
                    <!-- JMF Pending -->
                    <div class="subrow">
                        <h3>JMF</h3>
                        <em class="clear"><xsl:value-of select="@Size"/> Pending</em>
                        <!-- JMF Sent -->
                        <em class="clear"><xsl:value-of select="@NumSent"/> Sent</em>
                        <!-- JMF Queued -->
                        <em class="clear"><xsl:value-of select="@NumTry"/> Queued</em>
                        <!-- JMF Removed -->
                        <em class="clear"><xsl:value-of select="@NumRemove"/> Removed</em>
                    </div>
                  
                    <!-- Removed -->
                    <div class="subrow">
                        <!-- Fire & Forget Removed -->
                        <h3>Fire and Forget Removed</h3>
                        <em><xsl:value-of select="@NumRemoveFireForget"/></em>
                        <!-- JMF Errors Removed -->
                        <h3>JMF Errors Removed</h3>
                        <em><xsl:value-of select="@NumRemoveError"/></em>
                    </div>
              
                    <!-- Last Time ... -->
                    <div class="subrow">
                        <!-- Last time Sent -->
                        <h3>Last time Sent</h3>
                        <em><xsl:value-of select="@LastSent"/></em>
                        <!-- Last time Queued -->
                        <h3>Last time Queued</h3>
                        <em><xsl:value-of select="@LastQueued"/></em>
                    </div>
              
              
              </div> <!-- end firstRow -->
              <div class="secondRow"> 
                
                <!-- Status -->
                <div class="subrow">    
                    <h3>Status</h3>
                    <em>
                        <xsl:choose>
                          <xsl:when test="@Active='false'">
                            Down
                          </xsl:when>
                          <xsl:when test="@pause='true'">
                            Paused
                          </xsl:when>
                          <xsl:when test="@Size!='0'">
                            <xsl:choose>
                              <xsl:when test="@idle!='0'">
                                Dispatch errors
                              </xsl:when>
                              <xsl:otherwise>
                                Back log
                              </xsl:otherwise>
                            </xsl:choose>
                          </xsl:when>
                          <xsl:otherwise>
                            Active
                          </xsl:otherwise>
                        </xsl:choose>
                    </em>
             	</div><!-- subrow -->
              
                    <!-- Active since -->
                    <div class="subrow">
                        <h3>Active since</h3>
                        <em><xsl:value-of select="@CreationDate"/></em>
                    </div>
              
                    <!-- Show Sent Messages -->
                    <div class="subrow">
                        <h3>Show Sent Messages</h3>
                        <form>
                              <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
                              <input type="hidden" name="ListSenders" value="true"/>
                              <input type="hidden" name="URL">
                                <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
                              </input>
                              <input type="submit" value="List Senders" class="button"/>
                        </form>
                    </div>
              
                   <!-- Functions Buttons -->
                   <div class="subrow"> 
                   		<h3>Functions</h3>
                    <!-- pause / resume -->
                        <form>
                          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
                          <input type="hidden" name="URL">
                            <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
                          </input>
                          <xsl:choose>
                            <xsl:when test="@pause='true'">
                              <input type="hidden" name="pause" value="false"/>
                              <input type="submit" value="Resume" class="button"/>
                            </xsl:when>
                            <xsl:otherwise>
                              <input type="hidden" name="pause" value="true"/>
                              <input type="submit" value="Pause" class="button"/>
                            </xsl:otherwise>
                          </xsl:choose>
                        </form>
                    <!-- Remove Sender -->
                        <form>
                          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
                          <input type="hidden" name="StopSender" value="true"/>
                          <input type="hidden" name="URL">
                            <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
                          </input>
                          <input type="submit" value="Remove" class="button"/>
                        </form>
                    <!-- Flush unsent Messages -->
                        <form>
                          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
                          <input type="hidden" name="FlushSender" value="true"/>
                          <input type="hidden" name="URL">
                            <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
                          </input>
                          <input type="submit" value="Flush" class="button"/>
                        </form>
                   </div>
                	<div class="clear"></div>
            </div><!-- end secondRow -->
         <div class="clear"></div>
         </div><!-- end subscriptionDetails -->
        
     </div><!-- end box -->

  </xsl:template>
  <!--  end of template MessageSender  -->
  
  <!-- ######################################## -->
  
  <!-- Start of RemovedChannel -->
  <!-- 	This is basically just a message for the User that a Subscription has been removed, maybe there could be a message window for such.
  		This would make the message more noticable.
  		For now it is left at the bottom of the page. -->
  <xsl:template match="RemovedChannel">
    <div class="row">
        <li>
          Subscription
          <xsl:value-of select="@ChannelID"/>
          to
          <xsl:value-of select="@URL"/>
          has been removed.
        </li>
    </div>
  </xsl:template>
  <!--  end of template RemovedChannel  -->
  
  <!-- ######################################## -->
  
  <!-- Start of Message -->
  <!-- displayed after you pressed the List Senders Button in the Show Subscriptions Screen -->
  <xsl:template match="Message">
    <div class="row clear">
      
      
      	  <div class="subrow">

                    
      		<!-- Position -->
              <h3><xsl:value-of select="position()"/></h3>
                    
      		<!-- Time Stamp -->
			  <em>
              <xsl:choose>
                  <xsl:when test="xjdf:JMF/@TimeStamp">
                    <xsl:value-of select="xjdf:JMF/@TimeStamp"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="@TimeStamp"/>
                  </xsl:otherwise>
              </xsl:choose>
         	  </em>
              
              <!-- Status-bar -->
              <div>
                  <xsl:choose>
                    <!-- something is waiting -->
                    <xsl:when test="@Return='sent'">
                        <xsl:attribute name="class">status-bar green</xsl:attribute>
                    </xsl:when>
                    <xsl:when test="@Return='error'">
                        <xsl:attribute name="class">status-bar red</xsl:attribute>
                    </xsl:when>
                    <xsl:when test="@Return='removed'">
                        <xsl:attribute name="class">status-bar red</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="class">status-bar grey</xsl:attribute>
                    </xsl:otherwise>
                  </xsl:choose>
              </div><!-- end of Status-bar -->
         </div><!-- end of subrow -->
         
         
         <!-- Show Details Button -->
         <div class="subrow">     
              <form>
                <xsl:attribute name="action"><xsl:value-of select="/SubscriptionList/@Context"/>/showSubscriptions/<xsl:value-of select="/SubscriptionList/@DeviceID"/></xsl:attribute>
                <input type="hidden" name="ListSenders" value="true"/>
                <input type="hidden" name="URL">
                  <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
                </input>
                <input type="hidden" name="pos">
                  <xsl:attribute name="value"><xsl:value-of select="position()"/></xsl:attribute>
                </input>
                <input type="submit" value="Show Details" class="button"/>
              </form>
          </div><!-- end of subrow -->
      


      <!-- Return -->
      <div class="subrow">
      <h3>Return</h3>
        <xsl:value-of select="@Return"/>
      </div>
      
     <!-- URL -->
     <div class="subrow">
     	<h3>URL</h3>
        <xsl:value-of select="@URL"/>
     </div>
     <div class="clear"></div>
    </div> <!-- end of row -->
  </xsl:template>
  <!-- end of template Message -->
  
  <!-- ######################################## -->
  
  <xsl:include href="SubscriptionExtension.xsl"/>
	<xsl:include href="CPUTimer.xsl" />

</xsl:stylesheet>