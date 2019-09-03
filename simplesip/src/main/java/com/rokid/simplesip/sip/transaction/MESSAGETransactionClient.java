package com.rokid.simplesip.sip.transaction;

import android.text.TextUtils;

import com.rokid.simplesip.gb28181.DeviceItem;
import com.rokid.simplesip.gb28181.DeviceList;
import com.rokid.simplesip.gb28181.ResponseMessage;
import com.rokid.simplesip.gb28181.XMLUtil;
import com.rokid.simplesip.sip.address.NameAddress;
import com.rokid.simplesip.sip.address.SipURL;
import com.rokid.simplesip.sip.header.AuthorizationHeader;
import com.rokid.simplesip.sip.message.Message;
import com.rokid.simplesip.sip.message.MessageFactory;
import com.rokid.simplesip.sip.message.SipMethods;
import com.rokid.simplesip.sip.message.SipResponses;
import com.rokid.simplesip.sip.provider.SipProvider;
import com.rokid.simplesip.sip.provider.SipStack;
import com.rokid.simplesip.sip.provider.TransactionIdentifier;
import com.rokid.simplesip.tools.Logger;
import com.rokid.simplesip.ua.UserAgentProfile;

import java.util.ArrayList;
import java.util.List;

public class MESSAGETransactionClient extends Transaction {
    private boolean isMessage;
    private UserAgentProfile user_profile;
    private TransactionClientGB28181Listener listener;

    /**
     * Costructs a new Transaction
     *
     * @param sip_provider
     */
    public MESSAGETransactionClient(SipProvider sip_provider, TransactionClientGB28181Listener listener, UserAgentProfile user_profile) {
        super(sip_provider);
        this.user_profile = user_profile;
        this.listener = listener;
    }


    public void request() {
        sip_provider.addSipProviderListener(new TransactionIdentifier("MESSAGE"), this);
        //connection_id = sip_provider.sendMessage(request);
    }

    public void onReceivedMessage(SipProvider provider, Message msg) {
        Logger.i("xmlCallback", "receiveMessage");
        Logger.i("xmlCallback", msg.toString());

        if (msg.isRequest()) {
            String req_method = msg.getRequestLine().getMethod();
            if (req_method.equals(SipMethods.MESSAGE)) {
                isMessage = true;

                request = new Message(msg);
                connection_id = request.getConnectionId();
                transaction_id = request.getTransactionId();
               /* sip_provider.addSipProviderListener(transaction_id, this);
                sip_provider
                        .removeSipProviderListener(new TransactionIdentifier(
                                SipMethods.INVITE));*/
                String str = msg.getBody();

                Logger.i("xmlCallback", str);

                AuthorizationHeader ah = msg.getAuthorizationHeader();

                String sn = str.substring(str.indexOf("<SN>") + 4, str.indexOf("</SN>"));
                Message msg200 = MessageFactory.createResponse(
                        request, 200, SipResponses.reasonOf(200), null);
                String contact_user = request.getFromHeader().getNameAddress().getAddress().getUserName();

                String channelID = user_profile.username;
                //channelID = "33080002001326030001";

                String ipAddress = "144.34.221.165";
                int port = user_profile.video_port;
                if (!TextUtils.isEmpty(user_profile.contact_url)) {
                    ipAddress = new NameAddress(user_profile.contact_url).getAddress().getHost();
                    port = new NameAddress(user_profile.contact_url).getAddress().getPort();
                }

                String cmdType = XMLUtil.getSubUtilSimple(str, "<CmdType>(.*?)</CmdType>");
                Logger.d("Recv the MESSAGE cmdType=" + cmdType);

                if (cmdType.equals("Catalog")) {
                    DeviceItem item = new DeviceItem();
                    item.setDeviceID(channelID);
                    item.setName("testName");
                    item.setManufacturer("SYX");
                    item.setModel("SYX");
                    item.setOwner("SYX");
                    item.setCivilCode("110");
                    item.setAddress("ShangHai");
                    item.setParental("0");
                    item.setSafetyWay("0");
                    item.setRegisterWay("1");
                    item.setSecrecy("0");
                    item.setIPAddress(ipAddress);
                    item.setPort(String.valueOf(port));
                    item.setPassword(user_profile.passwd);
                    item.setStatus("ON");

                    List<DeviceItem> itemList = new ArrayList<>();
                    itemList.add(item);
                    DeviceList deviceList = new DeviceList();
                    deviceList.setDeviceList(itemList);
                    deviceList.setNum(String.valueOf(itemList.size()));

                    ResponseMessage responseMessage = new ResponseMessage();
                    responseMessage.setCmdType(cmdType);
                    responseMessage.setSN(sn);
                    responseMessage.setDeviceID(channelID);
                    responseMessage.setSumNum(String.valueOf(itemList.size()));
                    responseMessage.setDeviceList(deviceList);

                    String body = XMLUtil.convertBeanToXml(responseMessage);

                    Message messageRequest = MessageFactory.createMessageRequest(sip_provider,
                            request.getFromHeader().getNameAddress(),
                            request.getToHeader().getNameAddress(),
                            null,
                            XMLUtil.XML_MANSCDP_TYPE,
                            body
                    );
                    sip_provider.sendMessage(msg200, connection_id);
                    sip_provider.sendMessage(messageRequest, connection_id);
                    if (listener != null) {
                        listener.onTransGB28181SuccessResponse(this, request);
                    }
                } else if (cmdType.equals("DeviceInfo")) {

                    ResponseMessage responseMessage = new ResponseMessage();
                    responseMessage.setCmdType(cmdType);
                    responseMessage.setSN(sn);
                    responseMessage.setDeviceID(user_profile.username);
                    responseMessage.setDeviceType(SipStack.ua_info);
                    responseMessage.setManufacture("Rokid");
                    responseMessage.setModel("Rokid");
                    responseMessage.setFirmware("Rokidv2.1");
                    responseMessage.setResult("OK");

                    String body = XMLUtil.convertBeanToXml(responseMessage);

                    Message messageRequest = MessageFactory.createMessageRequest(sip_provider,
                            request.getFromHeader().getNameAddress(),
                            request.getToHeader().getNameAddress(),
                            null,
                            XMLUtil.XML_MANSCDP_TYPE,
                            body);
                    sip_provider.sendMessage(msg200, connection_id);
                    sip_provider.sendMessage(messageRequest, connection_id);

                } else if (cmdType.equals("Broadcast")) {

                    NameAddress contact = new NameAddress(new SipURL(contact_user,
                            sip_provider.getViaAddress(), sip_provider.getPort()));

                    ResponseMessage responseMessage = new ResponseMessage();
                    responseMessage.setCmdType(cmdType);
                    responseMessage.setSN(sn);
                    responseMessage.setDeviceID(user_profile.username);
                    responseMessage.setResult("OK");
                    String body = XMLUtil.convertBeanToXml(responseMessage);

                    Message messageRequest = MessageFactory.createMessageRequest(sip_provider,
                            request.getFromHeader().getNameAddress(),
                            request.getToHeader().getNameAddress(),
                            null,
                            XMLUtil.XML_MANSCDP_TYPE,
                            body
                    );

                    sip_provider.sendMessage(msg200, connection_id);
                    sip_provider.sendMessage(messageRequest, connection_id);

                } else if (cmdType.equals("DeviceControl")) {

                    NameAddress contact = new NameAddress(new SipURL(contact_user,
                            sip_provider.getViaAddress(), sip_provider.getPort()));

                    ResponseMessage responseMessage = new ResponseMessage();
                    responseMessage.setCmdType(cmdType);
                    responseMessage.setSN(sn);
                    responseMessage.setDeviceID(user_profile.username);
                    responseMessage.setResult("OK");
                    String body = XMLUtil.convertBeanToXml(responseMessage);

                    Message messageRequest = MessageFactory.createMessageRequest(sip_provider,
                            request.getFromHeader().getNameAddress(),
                            request.getToHeader().getNameAddress(),
                            null,
                            XMLUtil.XML_MANSCDP_TYPE,
                            body
                    );

                    sip_provider.sendMessage(msg200, connection_id);
                    sip_provider.sendMessage(messageRequest, connection_id);

                }


                //retransmission_to.halt();
            }
        }
    }

    @Override
    public void terminate() {

    }
}
