package org.bcos.evidence.sdkImpl;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jnr.ffi.Struct;
import org.fisco.bcos.channel.client.ChannelPushCallback;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.dto.ChannelPush;
import org.fisco.bcos.channel.dto.ChannelResponse;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.bcos.evidence.contract.Evidence;
import org.bcos.evidence.contract.EvidenceSignersData;
import org.bcos.evidence.contract.WesignMeshRoute;
import org.bcos.evidence.sdk.Callback;
import org.bcos.evidence.sdk.EvidenceData;
import org.bcos.evidence.sdk.EvidenceFace;
import org.bcos.evidence.utils.Tools;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint8;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.Hash;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.Sign;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple3;
import org.fisco.bcos.web3j.tuples.generated.Tuple7;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


public class EvidenceFaceImpl extends ChannelPushCallback implements EvidenceFace {
    static Logger logger = LoggerFactory.getLogger(EvidenceFaceImpl.class);

    @Override
    public String sha3(byte[] input) throws java.lang.IllegalArgumentException {
        if (input.length <= 0) {
            throw new java.lang.IllegalArgumentException();
        }
        byte[] output = Hash.sha3(input);
        return Numeric.toHexString(output, 0, output.length, false);
    }

    @Override
    public String allSha3(String json, List<String> hashs) throws java.lang.IllegalArgumentException {
        if (json == null || hashs == null) {
            throw new java.lang.IllegalArgumentException();
        }
        Collections.sort(hashs, new Comparator<String>() {
            public int compare(String a, String b) {
                return a.compareTo(b);
            }
        });
        Iterator<String> te = hashs.iterator();
        while (te.hasNext()) {
            json.concat(te.next());
        }

        return sha3(json.getBytes());
    }

    @Override
    public void loadPrivateKey(ECPrivateKey ecPrivateKey) {
        credentials = Credentials.create(ECKeyPair.create((ecPrivateKey).getS()));
    }

    @Override
    public void loadPrivateKey(String appid, ECPrivateKey ecPrivateKey) {
        meshCredentials.put(appid, Credentials.create(ECKeyPair.create((ecPrivateKey).getS())));
    }

    @Override
    public String getPublickey() {
        return credentials.getAddress();
    }

    @Override
    public String getPublickey(String appid) {
        if (meshCredentials.get(appid) == null) {
            return null;
        }

        return meshCredentials.get(appid).getAddress();
    }

    @Override
    public String signMessage(String evidenceHash) {
        try {
            Sign.SignatureData signatureData = Sign.getSignInterface().signMessage(evidenceHash.getBytes(), credentials.getEcKeyPair());
            return Tools.signatureDataToString(signatureData);
        } catch (RuntimeException e) {
            logger.error("signMessage message:{} Exception:{}", evidenceHash, e);
            throw e;
        }
    }

    @Override
    public String signMessage(String appid, String evidenceHash) {
        try {
            if (meshCredentials.get(appid) == null) {
                logger.error("get empty credentials, appid:{}", appid);
                return null;
            }

            Sign.SignatureData signatureData = Sign.getSignInterface().signMessage(evidenceHash.getBytes(), meshCredentials.get(appid).getEcKeyPair());
            return Tools.signatureDataToString(signatureData);
        } catch (RuntimeException e) {
            logger.error("signMessage message:{} Exception:{}", evidenceHash, e);
            throw e;
        }
    }

    @Override
    public String verifySignedMessage(String message, String signatureData) throws SignatureException, Exception {
        Sign.SignatureData signatureData1 = Tools.stringToSignatureData(signatureData);
        try {
            return "0x" + Keys.getAddress(Sign.signedMessageToKey(message.getBytes(), signatureData1));
        } catch (SignatureException e) {
            logger.error("verifySignedMessage message:{} sign:{} Exception:{}", message, signatureData, e);
            throw e;
        }
    }

    @Override
    public String verifySignedMessage(String appid, String evidenceHash, String signatureData) throws SignatureException, Exception {
        return verifySignedMessage(evidenceHash, signatureData);
    }

    @Override
    public EvidenceData getMessagebyHash(String address) throws InterruptedException, ExecutionException, Exception {
        logger.info("getMessagebyHash Address " + address);
        Evidence evidence = Evidence.load(address, web3j, credentials, BCConstant.gasPrice, BCConstant.gasLimit);
        EvidenceData evidenceData = new EvidenceData();
        try {
            Tuple7<String, String, String, List<BigInteger>, List<byte[]>, List<byte[]>, List<String>> result2 = evidence.getEvidence().send();
            if (result2 == null)
                return null;
            //证据字段为6个
            evidenceData.setEvidenceHash(result2.getValue1());
            evidenceData.setEvidenceInfo(result2.getValue2());
            evidenceData.setEvidenceID(result2.getValue3());
            List<BigInteger> vlist = result2.getValue4();
            List<byte[]> rlist = result2.getValue5();
            List<byte[]> slist = result2.getValue6();
            ArrayList<String> signatureList = new ArrayList<String>();
            for (int i = 0; i < vlist.size(); i++) {
                Sign.SignatureData signature = new Sign.SignatureData(
                        vlist.get(i).byteValue(), rlist.get(i), slist.get(i));
                signatureList.add(Tools.signatureDataToString(signature));
            }
            evidenceData.setSignatures(signatureList);
            List<String> addresses = result2.getValue7();
            ArrayList<String> addressesList = new ArrayList<String>();
            for (int i = 0; i < addresses.size(); i++) {
                String str = addresses.get(i);
                addressesList.add(str);
            }
            evidenceData.setPublicKeys(addressesList);
        } catch (InterruptedException e) {
            logger.error("getMessagebyHash Address Exception:", e);
            throw e;
        } catch (ExecutionException e) {
            logger.error("getMessagebyHash Address Exception", e);
            throw e;
        }
        return evidenceData;
    }

    @Override
    public EvidenceData getMessagebyHash(String appid, BigInteger seq, String address) throws InterruptedException, ExecutionException, Exception {
        logger.info("getMessagebyHash appid:{} Address:{}, seq:{}", appid, address, seq);

        EvidenceData evidenceData = new EvidenceData();
        try {

            byte[] appidBytes = appid.getBytes();
            if (appidBytes.length > 32) {
                logger.error("appid:{} to bytes size > 32", appid);
                return null;
            }

            byte[] id = new byte[32];
            System.arraycopy(appidBytes, 0, id, 0, appidBytes.length);
            WesignMeshRoute wesignMeshRoute = this.useMutilRoute ? this.mutilWesignMeshRoute.get(appid) : this.wesignMeshRoute;
            if (wesignMeshRoute == null) {
                logger.error("wesignMeshRoute get null.appid:{}", appid);
                return null;
            }
            byte[] setName = wesignMeshRoute.getRuleNodes(id, seq).sendAsync().get();
            if (setName == null || Tools.byte32ToString(setName) == null || Tools.byte32ToString(setName).equals("")) {
                logger.error("getRuleNodes error.appid:{}, seq:{}, setName:{}", appid, seq, Tools.byte32ToString(setName));
                return null;
            }


            if (this.meshWeb3js.get(appid) == null) {
                logger.error("not found appid:{} in meshWeb3js.", appid);
                return null;
            }
            String setNameStr = new String(setName).trim();
            Web3j web3j = this.meshWeb3js.get(appid).get(setNameStr);
            Credentials credentials = this.meshCredentials.get(appid);
            if (credentials == null) {
                logger.error("credentials is null");
            }
            Evidence evidence = Evidence.load(address, web3j, credentials, BCConstant.gasPrice, BCConstant.gasLimit);

            Tuple7<String, String, String, List<BigInteger>, List<byte[]>, List<byte[]>, List<String>> result2 = evidence.getEvidence().send();
            if (result2 == null) {
                logger.error("listFuture is null");
            }
            //证据字段为6个
            evidenceData.setEvidenceHash(result2.getValue1());
            evidenceData.setEvidenceInfo(result2.getValue2());
            evidenceData.setEvidenceID(result2.getValue3());
            List<BigInteger> vlist = result2.getValue4();
            List<byte[]> rlist = result2.getValue5();
            List<byte[]> slist = result2.getValue6();
            ArrayList<String> signatureList = new ArrayList<String>();
            for (int i = 0; i < vlist.size(); i++) {
                Sign.SignatureData signature = new Sign.SignatureData(
                        vlist.get(i).byteValue(), rlist.get(i), (slist.get(i)));
                signatureList.add(Tools.signatureDataToString(signature));
            }
            evidenceData.setSignatures(signatureList);
            List<String> addresses = result2.getValue7();
            ArrayList<String> addressesList = new ArrayList<String>();
            for (int i = 0; i < addresses.size(); i++) {
                String str = addresses.get(i);
                addressesList.add(str);
            }
            evidenceData.setPublicKeys(addressesList);

        } catch (InterruptedException e) {
            logger.error("getMessagebyHash Address InterruptedException", e);
            throw e;
        } catch (ExecutionException e) {
            logger.error("getMessagebyHash Address ExecutionException", address, e);
            throw e;
        } catch (UnsupportedEncodingException e) {
        }
        return evidenceData;
    }

    @Override
    public boolean sendSignatureToBlockChain(String address, String evidenceHash, String signatureData) throws InterruptedException, ExecutionException, SignatureException, Exception {
        logger.info("sendSignatureToBlockChain Address " + address);
        Evidence evidence = Evidence.load(address, web3j, credentials, BCConstant.gasPrice, BCConstant.gasLimit);
        Sign.SignatureData signature = Tools.stringToSignatureData(signatureData);
        try {
            String recoverAddress = verifySignedMessage(evidenceHash, signatureData);
            if (!getPublickey().equals(recoverAddress)) {
                logger.error("ERROR! sendSignatureToBlockChain recoverAddress:{} getPublicKey:{}", recoverAddress, getPublickey());
                throw new SignatureException();
            }

            TransactionReceipt receipt = evidence.addSignatures(BigInteger.valueOf(signature.getV()),
                    signature.getR(), signature.getS()).sendAsync().get();
            List<Evidence.AddSignaturesEventEventResponse> addList = evidence.getAddSignaturesEventEvents(receipt);
            List<Evidence.AddRepeatSignaturesEventEventResponse> addList2 = evidence.getAddRepeatSignaturesEventEvents(receipt);

            if (addList.size() > 0 || addList2.size() > 0) {
                return true;
            }
        } catch (InterruptedException e) {
            logger.error("sendSignatureToBlockChain Address:{} evi:{} sign:{} Exception:{}", address, evidenceHash, signatureData, e);
            throw e;
        } catch (ExecutionException e) {
            logger.error("sendSignatureToBlockChain Address:{} evi:{} sign:{} Exception:{}", address, evidenceHash, signatureData, e);
            throw e;
        }

        return false;
    }

    @Override
    public boolean sendSignatureToBlockChain(String appid, BigInteger seq, String address, String evidenceHash, String signatureData) throws InterruptedException, ExecutionException, SignatureException, Exception {
        logger.info("sendSignatureToBlockChain appid:{},Address:{}, seq:{}", appid, address, seq);
        byte[] appidBytes = appid.getBytes();
        if (appidBytes.length > 32) {
            logger.error("appid:{} to bytes size > 32", appid);
            return false;
        }

        byte[] id = new byte[32];
        System.arraycopy(appidBytes, 0, id, 0, appidBytes.length);


        WesignMeshRoute wesignMeshRoute = this.useMutilRoute ? this.mutilWesignMeshRoute.get(appid) : this.wesignMeshRoute;
        if (wesignMeshRoute == null) {
            logger.error("wesignMeshRoute get null.appid:{}", appid);
            return false;
        }

        RemoteCall<byte[]> future = wesignMeshRoute.getRuleNodes(id, seq);
        try {
            byte[] setName = future.send();
            String setNameStr = new String(setName).trim();
            if (setName == null || setName == null || setNameStr.equals("")) {
                logger.error("getRuleNodes error.appid:{}, seq:{}, setName:{}", appid, seq, setNameStr);
                return false;
            }

            if (this.meshWeb3js.get(appid) == null) {
                logger.error("not found appid:{} in meshWeb3js.", appid);
                return false;
            }

            Web3j web3j = this.meshWeb3js.get(appid).get(setNameStr);

            if (web3j == null) {
                logger.error("not found setName:{} in meshWeb3js.", setNameStr);
                return false;
            }

            Credentials credentials = this.meshCredentials.get(appid);

            Evidence evidence = Evidence.load(address, web3j, credentials, BCConstant.gasPrice, BCConstant.gasLimit);
            Sign.SignatureData signature = Tools.stringToSignatureData(signatureData);

            String recoverAddress = verifySignedMessage(evidenceHash, signatureData);
            if (!getPublickey(appid).equals(recoverAddress)) {
                logger.error("ERROR! sendSignatureToBlockChain recoverAddress:{} getPublicKey:{}", recoverAddress, getPublickey(appid));
                throw new SignatureException();
            }

            TransactionReceipt receipt = evidence.addSignatures(BigInteger.valueOf(signature.getV()), signature.getR(), signature.getS()).sendAsync().get();
            List<Evidence.AddSignaturesEventEventResponse> addList = evidence.getAddSignaturesEventEvents(receipt);
            List<Evidence.AddRepeatSignaturesEventEventResponse> addList2 = evidence.getAddRepeatSignaturesEventEvents(receipt);
            List<Evidence.ErrorRepeatSignaturesEventEventResponse> errorRepeat = evidence.getErrorRepeatSignaturesEventEvents(receipt);
            List<Evidence.ErrorAddSignaturesEventEventResponse> errorAdd = evidence.getErrorAddSignaturesEventEvents(receipt);
            if (addList.size() > 0 || addList2.size() > 0) {
                logger.info("addSignatures ok");
                return true;
            } else if (errorRepeat.size() > 0) {
                logger.error("addSignatures failed.errorRepeat size:{}", errorRepeat.size());
                return false;
            } else if (errorAdd.size() > 0) {
                logger.error("addSignatures failed. errorAdd size:{},msg.sender:{}", errorAdd.size(), errorAdd.get(0).addr);
                return false;
            }

            return false;
        } catch (Exception e) {
            logger.error("sendSignatureToBlockChain Exception", e);
        }

        return false;
    }

    @Override
    public void setPushCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            service.run();
            PushCallback pushCallback = new PushCallback();
            pushCallback.setSDK(this);
            service.setPushCallback(pushCallback);
            Thread.sleep(3000);//此处为了等待service初始化完
        } catch (InterruptedException e) {
            logger.error("run InterruptedException:{}" + e);
        } catch (Exception e) {
            logger.error("run Exception:{}" + e);
        }
    }

    @Override
    public void run(String appid, int groupId) {
        logger.debug("start running appid:{}", appid);
        Service serviceRoute = this.useMutilRoute ? this.mutilServiceRoute.get(appid) : this.serviceRoute;
        WesignMeshRoute wesignMeshRoute = this.useMutilRoute ? this.mutilWesignMeshRoute.get(appid) : this.wesignMeshRoute;

        if (serviceRoute == null || wesignMeshRoute == null) {
            logger.error("serviceRoute|wesignMeshRoute get null.appid:{}", appid);
            return;
        }

        byte[] appidBytes = appid.getBytes();
        byte[] orgBytes = serviceRoute.getOrgID().getBytes();
        if (appidBytes.length > 32 || orgBytes.length > 32) {
            logger.error("appid:{}, org:{} to bytes size > 32", appid, serviceRoute.getOrgID());
        } else {
            logger.debug("run method serviceRoute's orgID:{}", serviceRoute.getOrgID());
            byte[] id = new byte[32];
            System.arraycopy(appidBytes, 0, id, 0, appidBytes.length);
            byte[] orgBytesNew = new byte[32];
            System.arraycopy(orgBytes, 0, orgBytesNew, 0, orgBytes.length);
            RemoteCall<Tuple3<List<byte[]>, List<BigInteger>, List<byte[]>>> future = wesignMeshRoute.getAppidNodes(id, orgBytesNew);
            try {
                Tuple3<List<byte[]>, List<BigInteger>, List<byte[]>> result = future.send();
                if (result == null) {
                    logger.error("getAppidNodes result is null or size error, result is null:{}, size:{}, appid:{}", result == null ? 1 : 0, result == null ? 0 : 3, appid);
                    return;
                }

                List<byte[]> ips = result.getValue1();
                List<BigInteger> channelPorts = result.getValue2();
                List<byte[]> sets = result.getValue3();
                if (ips.size() != channelPorts.size() || ips.size() != sets.size()) {
                    logger.error("ips'size != channelPorts'size");
                    return;
                }

                int len = ips.size();
                Map<String, Map<String, Boolean>> setIpPortMap = new HashMap<>();

                for (int i = 0; i < len; i++) {
                    String setName = new String(sets.get(i)).trim();
                    String ip = new String(ips.get(i)).trim();
                    BigInteger channelPort = channelPorts.get(i);

                    if (setName == null || setName.equals("") || ip == null || ip.equals("") || channelPort.intValue() == 0) {
                        //logger.warn("get null or empty value.setName:{}. ip:{}, channelPort:{}", setName, ip, channelPort);
                        continue;
                    }

                    if (setIpPortMap.containsKey(setName)) {
                        Map<String, Boolean> ipPortMap = setIpPortMap.get(setName);
                        ipPortMap.put(ip + ":" + channelPort.toString(), true);
                        setIpPortMap.put(setName, ipPortMap);

                    } else {
                        Map<String, Boolean> ipPortMap = new HashMap<>();
                        ipPortMap.put(ip + ":" + channelPort.toString(), true);
                        setIpPortMap.put(setName, ipPortMap);
                    }
                }

                for (String setName : setIpPortMap.keySet()) {
                    ///Service service = new Service();
                    Service service = serviceRoute;
                    service.setOrgID(setName);
                    logger.debug("#### service for connnection: {}", service);
                    List<String> topics_list = this.useMutilRoute ? this.mutilTopics.get(appid) : this.topics;
                    Set topics = new HashSet(topics_list);
                    if (topics != null) {
                        service.setTopics(topics);
                    }
                    logger.debug("##### topic: {}, appid: {}" , topics.toString(), appid);

                    ConcurrentHashMap<String, ChannelConnections> allChannelConnections = new ConcurrentHashMap<>();
                    ChannelConnections connections = new ChannelConnections();
                    List<String> connectionStrList = new ArrayList<>();
                    for (String ipPort : setIpPortMap.get(setName).keySet()) {
                        logger.debug("run method connections ipPort: {}", ipPort);
                        String str = ipPort;
                        connectionStrList.add(str);
                    }

//                    connections.setConnectionsStr(connectionStrList);
//                    allChannelConnections.put(setName, connections);
//                    service.setAllChannelConnections(allChannelConnections);

                    ChannelConnections channelConnections = new ChannelConnections();

                    channelConnections.setConnectionsStr(connectionStrList);
                    channelConnections.setGroupId(groupId);
                    List<ChannelConnections> channelConnectionsList = new ArrayList<>();
                    channelConnectionsList.add(channelConnections);
                    GroupChannelConnectionsConfig groupChannelConnectionsConfig= new GroupChannelConnectionsConfig();
                    groupChannelConnectionsConfig.setAllChannelConnections(channelConnectionsList);
                    service.setAllChannelConnections(groupChannelConnectionsConfig);
                    service.setThreadPool(serviceRoute.getThreadPool());//共用

                    service.setPushCallback(this);

                    service.run();
                    logger.info("#### before update meshWeb3js");
                    if (!this.meshServices.containsKey(appid)) {
                        logger.info("### put web3j for: " + appid);
                        Map<String, Service> setService = new HashMap<>();
                        setService.put(setName, service);
                        this.meshServices.put(appid, setService);
                        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
                        channelEthereumService.setChannelService(service);
                        Map<String, Web3j> setWeb3j = new HashMap<>();
                        setWeb3j.put(setName, Web3j.build(channelEthereumService, groupId));
                        this.meshWeb3js.put(appid, setWeb3j);
                    } else {
                        Map<String, Service> setService = this.meshServices.get(appid);
                        Map<String, Web3j> setWeb3j = this.meshWeb3js.get(appid);
                        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
                        channelEthereumService.setChannelService(service);

                        setService.put(setName, service);
                        setWeb3j.put(setName, Web3j.build(channelEthereumService, groupId));

                        this.meshServices.put(appid, setService);
                        this.meshWeb3js.put(appid, setWeb3j);
                    }
                }

                Thread.sleep(3000);//此处为了等待service初始化完
            } catch (Exception e) {
                logger.error("getAppidNodes exception", e);
            }
        }

        Map<String, Service> serviceMap = this.meshServices.get(appid);
        if (serviceMap != null) {
            logger.debug("### serviceMap is not null");
            for (String setName : serviceMap.keySet()) {
                logger.error("### serviceMap, setName: {}", setName);
                Service service = serviceMap.get(setName);
                logger.error("### serviceMap, service: {}", service);
                service.setPushCallback(this);
            }
        }
        else
        {
            logger.debug("### serviceMap is null");
        }
    }

    @Override
    public void initRoute(Service service, Web3j web3j, ECPrivateKey ecPrivateKey, String address, List<String> topics) {
        this.serviceRoute = service;
        this.web3jRoute = web3j;
        this.credentialsRoute = Credentials.create(ECKeyPair.create((ecPrivateKey).getS()));
        this.routeAddress = address;
        this.topics = topics;

        try {
            Set topics_set = new HashSet(topics);
            this.serviceRoute.setTopics(topics_set);
            this.serviceRoute.setPushCallback(this);
            this.serviceRoute.run();
            Thread.sleep(3000);
        } catch (Exception e) {
            logger.error("Exception", e);
        }

        this.wesignMeshRoute = WesignMeshRoute.load(this.routeAddress, this.web3jRoute, this.credentialsRoute, BCConstant.gasPrice, BCConstant.gasLimit);
    }

    @Override
    public void initAppidRoute(String appid, Service service, Web3j web3j, ECPrivateKey ecPrivateKey, String address, List<String> topics) {
        this.mutilServiceRoute.put(appid, service);
        this.mutilWeb3jRoute.put(appid, web3j);
        this.mutilCredentialsRoute.put(appid, Credentials.create(ECKeyPair.create((ecPrivateKey).getS())));
        this.mutilRouteAddress.put(appid, address);
        this.mutilTopics.put(appid, topics);
        logger.debug("#### put topic: topic = {}, appid = {}", topics.toString(), appid);

        try {
            Set topics_set = new HashSet(topics);
            service.setTopics(topics_set);
            service.setPushCallback(this);
            service.run();
            Thread.sleep(3000);
        } catch (Exception e) {
            logger.error("Exception", e);
        }

        this.useMutilRoute = true;
        this.mutilWesignMeshRoute.put(appid, WesignMeshRoute.load(address, web3j, Credentials.create(ECKeyPair.create((ecPrivateKey).getS())), BCConstant.gasPrice, BCConstant.gasLimit));
    }


    @Override
    public void onPush(ChannelPush push) {
        logger.debug("###onPush, messageID:{}, content:{}", push.getMessageID(), push.getContent());
        ChannelResponse response = new ChannelResponse();
        if (callback != null) {
            try {
                JSONObject jsonObject = JSON.parseObject(push.getContent());
                if (jsonObject.containsKey("appid") && jsonObject.containsKey("evidenceAddress") && jsonObject.containsKey("seq")) {
                    logger.debug("#### onPush to appid");

                    this.callback.onPush(jsonObject.getString("appid"), jsonObject.getBigInteger("seq"), jsonObject.getString("evidenceAddress"));
                    logger.debug("#### after onPush to appid");
                } else {
                    logger.debug("#### onPush to content");
                    this.callback.onPush(push.getContent());
                    logger.debug("#### after onPush to content");
                }
                response.setErrorCode(0);
            } catch (Exception e) {
                logger.error("### catch Exception, content:" + push.getContent(), e);
                this.callback.onPush(push.getContent());
                logger.error("after catch Exception, content:" + push.getContent(), e);
                response.setErrorCode(-1);
            }
        }
        response.setContent("receive request seq:" + String.valueOf(push.getMessageID()) + ", content:" + push.getContent());
        push.sendResponse(response);
        logger.debug("### callback EvidenceFaceImpl extend ChannelPushCallback: after sendResponse");
    }

    @Override
    public Address newEvidence(String evi, String info, String id, String signatureDataString, String sender)
            throws InterruptedException, ExecutionException, SignatureException, Exception {
        Sign.SignatureData signatureData = Tools.stringToSignatureData(signatureDataString);
        try {
            EvidenceSignersData evidenceSignersData = SingletonEvidenceSignersData.getInstance().getEvidenceSignersData();
            TransactionReceipt receipt = evidenceSignersData.newEvidence(evi, info, id,
                    BigInteger.valueOf(signatureData.getV()), signatureData.getR(),
                    signatureData.getS(), sender).sendAsync().get();
            List<EvidenceSignersData.NewEvidenceEventEventResponse> newEvidenceList = evidenceSignersData.getNewEvidenceEventEvents(receipt);
            if (newEvidenceList.size() > 0) {
                return new Address(newEvidenceList.get(0).addr);
            } else {
                logger.error("newEvidence newEvidenceList = 0 evi:{} info:{} sign:{} sender:{}", evi, info, signatureDataString, sender);
                return null;
            }
        } catch (InterruptedException e) {
            logger.error("newEvidence evi:{} info:{} sign:{} Exception:{}", evi, info, signatureDataString, e);
            throw e;
        } catch (ExecutionException e) {
            logger.error("newEvidence evi:{} info:{} sign:{} Exception:{}", evi, info, signatureDataString, e);
            throw e;
        }
    }

    @Override
    public Address newEvidence(String evi, String info, String id, String signatureDataString)
            throws InterruptedException, ExecutionException, SignatureException, Exception {
        Sign.SignatureData signatureData = Tools.stringToSignatureData(signatureDataString);
        try {
            String recoverAddress = verifySignedMessage(evi, signatureDataString);
            if (!getPublickey().equals(recoverAddress)) {
                logger.error("ERROR! newEvidence recoverAddress:{} getPublicKey:{}", recoverAddress, getPublickey());
                throw new SignatureException();
            }
            EvidenceSignersData evidenceSignersData = SingletonEvidenceSignersData.getInstance().getEvidenceSignersData();
            TransactionReceipt receipt = evidenceSignersData.newEvidence(evi, info, id, BigInteger.valueOf(signatureData.getV()),
                    signatureData.getR(), signatureData.getS()).sendAsync().get();
            List<EvidenceSignersData.NewEvidenceEventEventResponse> newEvidenceList = evidenceSignersData.getNewEvidenceEventEvents(receipt);
            if (newEvidenceList.size() > 0) {
                return new Address(newEvidenceList.get(0).addr);
            } else {
                logger.error("newEvidence newEvidenceList = 0 evi:{} info:{} sign:{}", evi, info, signatureDataString);
                return null;
            }
        } catch (InterruptedException e) {
            logger.error("newEvidence evi:{} info:{} sign:{} Exception:{}", evi, info, signatureDataString, e);
            throw e;
        } catch (ExecutionException e) {
            logger.error("newEvidence evi:{} info:{} sign:{} Exception:{}", evi, info, signatureDataString, e);
            throw e;
        }
    }

    @Override
    public Address newEvidence(String appid, BigInteger seq, String evi, String info, String id, String signatureDataString) throws InterruptedException, ExecutionException, SignatureException, Exception {
        Sign.SignatureData signatureData = Tools.stringToSignatureData(signatureDataString);
        try {
            String recoverAddress = verifySignedMessage(evi, signatureDataString);
            if (!getPublickey(appid).equals(recoverAddress)) {
                logger.error("ERROR! newEvidence recoverAddress:{} getPublicKey:{}", recoverAddress, getPublickey(appid));
                throw new SignatureException();
            }

            byte[] appidBytes = new byte[32];
            System.arraycopy(appid.getBytes(), 0, appidBytes, 0, appid.getBytes().length);
            logger.debug("#### appid: " + appid);
            logger.debug("#### appidBytes:" + (new String(appidBytes).trim()));
            if (appidBytes.length > 32) {
                logger.error("appid:{} to bytes size > 32", appid);
                return null;
            }

            WesignMeshRoute wesignMeshRoute = this.useMutilRoute ? this.mutilWesignMeshRoute.get(appid) : this.wesignMeshRoute;
            if (wesignMeshRoute == null) {
                logger.error("wesignMeshRoute get null.appid:{}", appid);
                return null;
            }

            RemoteCall<byte[]> future = wesignMeshRoute.getRuleNodes(appidBytes, seq);
            byte[] setName = future.send();
            String setNameStr = new String(setName).trim();
            String appidBytesStr = new String(appidBytes).trim();
            logger.debug("appid:{}, seq:{}, setName:{}", appidBytesStr, seq, setNameStr);

            if (setName == null || setNameStr.equals("")) {
                logger.error("getRuleNodes error.appid:{}, seq:{}, setName:{}", appid, seq, setNameStr);
                return null;
            }
            RemoteCall<String> futureAddr = wesignMeshRoute.getEvidenceFactoryAddr(setName);
            String factoryAddr = futureAddr.send();
            if (factoryAddr == null) {
                logger.error("getEvidenceFactoryAddr get null");
                return null;
            }
            logger.debug("factoryAddr:{},setName:{}, seq:{}, appid:{}", factoryAddr, setNameStr, seq, appid);
            if (this.meshWeb3js.get(appid) == null) {
                logger.error("not found appid:{} in meshWeb3js.", appid);
                return null;
            }
            EvidenceSignersData evidenceSignersData = EvidenceSignersData.load(factoryAddr, this.meshWeb3js.get(appid).get(setNameStr), this.meshCredentials.get(appid), BCConstant.gasPrice, BCConstant.gasLimit);

            TransactionReceipt receipt = evidenceSignersData.newEvidence(evi, info, id,
                    BigInteger.valueOf(signatureData.getV()), signatureData.getR(), signatureData.getS()).send();
            List<EvidenceSignersData.NewEvidenceEventEventResponse> newEvidenceList = evidenceSignersData.getNewEvidenceEventEvents(receipt);
            if (newEvidenceList.size() > 0) {
                return new Address(newEvidenceList.get(0).addr);
            } else {
                logger.error("newEvidence newEvidenceList = 0 appid:{} seq:{} evi:{} info:{} sign:{}", appid, seq, evi, info, signatureDataString);
                return null;
            }
        } catch (InterruptedException e) {
            logger.error("newEvidence evi:{} info:{} sign:{} Exception:{}", evi, info, signatureDataString, e);
            throw e;
        } catch (ExecutionException e) {
            logger.error("newEvidence evi:{} info:{} sign:{} Exception:{}", evi, info, signatureDataString, e);
            throw e;
        } catch (UnsupportedEncodingException e) {
            logger.error("newEvidence UnsupportedEncodingException, appid:{}, seq:{}, evi:{}, info:{}, sign:{}", appid, seq, evi, info, signatureData, e);
        }

        return null;
    }

    @Override
    public Address newEvidence(String appid, BigInteger seq, String evi, String info, String id, String signatureDataString, String sender) throws InterruptedException, ExecutionException, SignatureException, Exception {
        Sign.SignatureData signatureData = Tools.stringToSignatureData(signatureDataString);
        try {
            sender = verifySignedMessage(appid, evi, signatureDataString);
            logger.info("newEvidence appid: {} seq: {} calcPublickey: {} getPublickey: {}", appid, seq, sender, getPublickey(appid));

            byte[] appidBytes = appid.getBytes();
            if (appidBytes.length > 32) {
                logger.error("appid:{} to bytes size > 32", appid);
                return null;
            }

            WesignMeshRoute wesignMeshRoute = this.useMutilRoute ? this.mutilWesignMeshRoute.get(appid) : this.wesignMeshRoute;
            if (wesignMeshRoute == null) {
                logger.error("wesignMeshRoute get null.appid:{}", appid);
                return null;
            }

            RemoteCall<byte[]> future = wesignMeshRoute.getRuleNodes(appidBytes, seq);
            byte[] setName = future.send();
            logger.debug("appid:{}, seq:{}, setName:{}", appid, seq, setName);
            String setNameStr = new String(setName).trim();
            if (setName == null || setName == null || setNameStr.equals("")) {
                logger.error("getRuleNodes error.appid:{}, seq:{}, setName:{}", appid, seq, setNameStr);
                return null;
            }

            RemoteCall<String> futureAddr = wesignMeshRoute.getEvidenceFactoryAddr(setName);
            String factoryAddr = futureAddr.send();
            if (factoryAddr == null) {
                logger.error("getEvidenceFactoryAddr get null");
                return null;
            }
            logger.debug("factoryAddr:{},setName:{}, seq:{}, appid:{}", factoryAddr, setNameStr, seq, appid);
            if (this.meshWeb3js.get(appid) == null) {
                logger.error("not found appid:{} in meshWeb3js.", appid);
                return null;
            }
            EvidenceSignersData evidenceSignersData = EvidenceSignersData.load(factoryAddr, this.meshWeb3js.get(appid).get(setNameStr), this.meshCredentials.get(appid), BCConstant.gasPrice, BCConstant.gasLimit);

            TransactionReceipt receipt = evidenceSignersData.newEvidence(evi, info, id,
                    BigInteger.valueOf(signatureData.getV()), signatureData.getR(),
                    signatureData.getS(),
                    sender).sendAsync().get();
            List<EvidenceSignersData.NewEvidenceEventEventResponse> newEvidenceList = evidenceSignersData.getNewEvidenceEventEvents(receipt);
            if (newEvidenceList.size() > 0) {
                return new Address(newEvidenceList.get(0).addr);
            } else {
                logger.error("newEvidence newEvidenceList = 0 appid:{} seq:{} evi:{} info:{} sign:{} sender:{}", appid, seq, evi, info, signatureDataString, sender);
                return null;
            }
        } catch (InterruptedException e) {
            logger.error("newEvidence evi:{} info:{} sign:{} Exception:{}", evi, info, signatureDataString, e);
            throw e;
        } catch (ExecutionException e) {
            logger.error("newEvidence evi:{} info:{} sign:{} Exception:{}", evi, info, signatureDataString, e);
            throw e;
        } catch (UnsupportedEncodingException e) {
            logger.error("newEvidence UnsupportedEncodingException, appid:{}, seq:{}, evi:{}, info:{}, sign:{}", appid, seq, evi, info, signatureData, e);
        }

        return null;
    }

    @Override
    public void setService(Service service) {
        this.service = service;
    }

    @Override
    public Web3j getWebj() {
        return web3j;
    }


    @Override
    public Service getService() {
        return service;
    }

    @Override
    public Service getService(String appid, BigInteger seq) {
        if (this.meshServices.get(appid) == null) {
            return null;
        }

        try {
            byte[] id = new byte[32];
            System.arraycopy(appid.getBytes(), 0, id, 0, appid.getBytes().length);

            WesignMeshRoute wesignMeshRoute = this.useMutilRoute ? this.mutilWesignMeshRoute.get(appid) : this.wesignMeshRoute;
            if (wesignMeshRoute == null) {
                logger.error("wesignMeshRoute get null.appid:{}", appid);
                return null;
            }
            RemoteCall<byte[]> future = wesignMeshRoute.getRuleNodes(id, seq);
            byte[] setName = future.send();
            String setNameStr = new String(setName).trim();
            if (setName == null || setName == null || setNameStr.equals("")) {
                logger.error("getRuleNodes error.appid:{}, seq:{}, setName:{}", appid, seq, setNameStr);
                return null;
            }
            return this.meshServices.get(appid).get(setNameStr);
        } catch (Exception e) {
            logger.error("getService Exception appid:{}, seq:{}", appid, seq);
            return null;
        }
    }

    @Override
    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }

    @Override
    public boolean verifyEvidence(EvidenceData data) throws SignatureException, Exception {
        ArrayList<String> addressList = new ArrayList<>();
        for (String str : data.getSignatures()) {
            try {
                addressList.add(verifySignedMessage(data.getEvidenceHash(), str));
            } catch (SignatureException e) {
                logger.error("verifySignedMessage message:{} sign:{} Exception:{}", data.getEvidenceHash(), str, e);
                throw e;
            }
        }
        for (String addr : data.getPublicKeys()) {
            boolean flag = false;
            for (String str : addressList) {
                if (str.equals(addr)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean verifyEvidence(String appid, EvidenceData data) throws SignatureException, Exception {
        return this.verifyEvidence(data);
    }

    @Override
    public void createSignersDataInstance(String contractAddress) {
        SingletonEvidenceSignersData.createInstance(contractAddress, web3j, credentials);
    }


    private Credentials credentials;
    private Callback callback;
    private Service service;
    private Web3j web3j;

    /**
     * meshchain的，单个路由
     */

    private Credentials credentialsRoute;
    private Service serviceRoute;
    private Web3j web3jRoute;
    private WesignMeshRoute wesignMeshRoute;
    private String routeAddress;
    private List<String> topics;

    /**
     * 配置多个路由
     */
    private Map<String, Credentials> mutilCredentialsRoute = new HashMap<>();
    private Map<String, Service> mutilServiceRoute = new HashMap<>();
    private Map<String, Web3j> mutilWeb3jRoute = new HashMap<>();
    private Map<String, WesignMeshRoute> mutilWesignMeshRoute = new HashMap<>();
    private Map<String, String> mutilRouteAddress = new HashMap<>();
    private Map<String, List<String>> mutilTopics = new HashMap<>();

    private boolean useMutilRoute = false;

    private Map<String, Credentials> meshCredentials = new HashMap<>();
    private Map<String, Map<String, Callback>> meshCallbacks = new HashMap<>();
    private Map<String, Map<String, Service>> meshServices = new HashMap<>();
    private Map<String, Map<String, Web3j>> meshWeb3js = new HashMap<>();

    private ThreadPoolTaskExecutor threadPool;

}
