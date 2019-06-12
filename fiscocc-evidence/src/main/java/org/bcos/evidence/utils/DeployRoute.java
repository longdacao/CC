package org.bcos.evidence.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.bcos.evidence.contract.EvidenceSignersData;
import org.bcos.evidence.contract.WesignMeshRoute;
import org.bcos.evidence.sdkImpl.BCConstant;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple3;
import org.fisco.bcos.web3j.tuples.generated.Tuple4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by suyuhui on 18/1/16.
 */
public class DeployRoute {
	static Logger logger = LoggerFactory.getLogger(DeployRoute.class);
    private byte[] orgNameBytes;

    /**
     * @desc 读取json文件,格式如下
     * @param fileName
     * @return JSONObject
     * @throws IOException
     */
    public static JSONObject readJSONFile(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder sb = new StringBuilder();
        String tmp = null;
        while((tmp = reader.readLine()) != null) {
            sb.append(tmp);
        }

        reader.close();
        JSONObject jsonObject = JSON.parseObject(sb.toString());
        return jsonObject;
    }

    public static void deleteRules(Service service, String routeAddress, String appid, String ruleName) throws Exception {
    	logger.debug("start handle deleteRules...");
    	
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        Web3j web3 = Web3j.build(channelEthereumService, service.getGroupId());

        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream =  DeployEvidenceSignersData.class.getClassLoader().getResourceAsStream("szt.jks");
        ks.load(ksInputStream, "123456".toCharArray());
        Key key = ks.getKey("ec", "123456".toCharArray());
        ECKeyPair keyPair = ECKeyPair.create(((ECPrivateKey) key).getS());

        Credentials credentials = Credentials.create(keyPair);
        WesignMeshRoute wesignMeshRoute = WesignMeshRoute.load(routeAddress, web3, credentials, BCConstant.gasPrice, BCConstant.gasLimit);

        byte[] ruleNameBytes = ruleName.getBytes();
        if (ruleNameBytes.length > 32) {
            System.out.printf("ruleName:%s to bytes size > 32\n", ruleName);
            return;
        }

        ///byte[] rule = new byte[32];
        ///System.arraycopy(ruleNameBytes, 0, rule, 0, ruleNameBytes.length);

        byte[] appidBytes = appid.getBytes();
        if (appidBytes.length > 32) {
            System.out.printf("appid:%s to bytes size > 32\n", appid);
            return;
        }

        ///byte[] id = new byte[32];
        ///System.arraycopy(appidBytes, 0, id, 0, appidBytes.length);

        TransactionReceipt transactionReceipt = wesignMeshRoute.deleteRule(appidBytes, ruleNameBytes).sendAsync().get();

        WesignMeshRoute meshRoute = WesignMeshRoute.load("", web3, credentials, BCConstant.gasPrice, BCConstant.gasLimit);
        List<WesignMeshRoute.DelRuleLogEventResponse> logList = meshRoute.getDelRuleLogEvents(transactionReceipt);
        if (logList != null && logList.size() == 1) {
            WesignMeshRoute.DelRuleLogEventResponse delLog = logList.get(0);
            if (delLog.code.intValue() == 0) {
                System.out.println("delete ok.");
                getAllRules(service, routeAddress, appid);
            } else {
                System.out.println("delete failed.err msg:" + delLog.msg);
                return;
            }
        }

    }


    public static void getAllRules(Service service, String routeAddress, String appid) throws IOException, InterruptedException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, ExecutionException, Exception {
    	logger.debug("start handle getAllRules...");
    	
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        logger.info("## getAllRules, group:{}", service.getGroupId());
        Web3j web3 = Web3j.build(channelEthereumService, service.getGroupId());

        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream =  DeployEvidenceSignersData.class.getClassLoader().getResourceAsStream("szt.jks");
        ks.load(ksInputStream, "123456".toCharArray());
        Key key = ks.getKey("ec", "123456".toCharArray());
        ECKeyPair keyPair = ECKeyPair.create(((ECPrivateKey) key).getS());

        Credentials credentials = Credentials.create(keyPair);
        WesignMeshRoute wesignMeshRoute = WesignMeshRoute.load(routeAddress, web3, credentials, BCConstant.gasPrice, BCConstant.gasLimit);

        byte[] appidBytes = appid.getBytes();
        if (appidBytes.length > 32) {
            System.out.printf("appid:%s to bytes size > 32\n", appid);
            return;
        }

        byte[] id = new byte[32];
        System.arraycopy(appidBytes, 0, id, 0, appidBytes.length);
        Tuple4<List<byte[]>, List<BigInteger>, List<BigInteger>, List<byte[]>> typeList = wesignMeshRoute.getAllRules(id).send();
        if(typeList == null)
        {
            System.out.println("getAllRules result error.");
            System.exit(0);
        }

        if ("[]".equals(typeList.getValue1())) {
            System.out.printf("There is no rules for %s by routeAddress:%s. \n", appid, routeAddress);
            return;
        }
        List<byte[]> ruleNames = typeList.getValue1();
        List<BigInteger> seqMins = typeList.getValue2();
        List<BigInteger> seqMaxs = typeList.getValue3();
        List<byte[]> setTmps = typeList.getValue4();

            int setIndex = 0;

            if (ruleNames.size() == seqMins.size()
                    && seqMins.size() == seqMaxs.size()) {
                int len = ruleNames.size();
                String format = "%-20s\t%-20s\t%-20s\t%-20s\n";

                if (len > 0) {
                	System.out.println("\n" + appid + "'s current route :");
                    System.out.printf(format, "ruleName", "seqMin", "seqMax", "sets");
                }

                for (int i = 0; i < len; i++) {
                    byte[] ruleName = ruleNames.get(i);
                    BigInteger seqMin = seqMins.get(i);
                    BigInteger seqMax = seqMaxs.get(i);
                    String setNames = "";

                    for (int j = setIndex; j < setTmps.size(); j++, setIndex++) {
                        String setNameStr = setTmps.get(j).toString();
                        if (setNameStr.equals("|")) {
                            setIndex++;
                            break;
                        } else {
                            setNames = setNames + setNameStr + ",";
                        }

                    }

                    if (setNames.endsWith(",")) {
                        setNames = setNames.substring(0, setNames.length() - 1);
                    }

                    System.out.printf(format, ruleName.toString(), seqMin.intValue(),
                            seqMax.intValue(), setNames);
                }
            } else {
                System.out.println("ruleNames, seqMins, seqMaxs length not match.");
                System.exit(0);
            }

    }

    public static void deployOrInserRoute(Service service, String []args) throws Exception  {
    	logger.debug("start handle deployOrInserRoute...");
    	
    	if (args.length <= 0) {
            System.out.println("miss json file for deploy/insert wesign route.");
            System.exit(0);
        }

        String routeAddress = null;

        if (args.length == 1) {

        } else if (args.length == 2) {
            routeAddress = args[1];
        } else {
            System.out.println("args' length error");
            return;
        }

        String fileName = args[0];
        JSONObject jsonObject = readJSONFile(fileName);
        if (!jsonObject.containsKey("nodes") || !jsonObject.containsKey("orgs") ||
                !jsonObject.containsKey("sets") || !jsonObject.containsKey("rules")) {
            System.out.println("json file miss key:nodes,orgs,sets,rules");
            System.exit(0);
        }

        JSONArray nodes = jsonObject.getJSONArray("nodes");
        JSONArray orgs = jsonObject.getJSONArray("orgs");
        JSONArray sets = jsonObject.getJSONArray("sets");
        JSONArray rules = jsonObject.getJSONArray("rules");
        JSONArray groupIdArray = jsonObject.getJSONArray("groups");
        JSONObject groupIdObj = JSON.parseObject(groupIdArray.get(0).toString()); 
        int groupId = groupIdObj.getInteger("groupId");

        logger.debug("### groupId: {}", groupId);

        /*ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service service = context.getBean(Service.class);
        service.setOrgID("WB");
        service.run();

        Thread.sleep(3000);*/
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        service.setGroupId(groupId); 
        Web3j web3 = Web3j.build(channelEthereumService, groupId);
        
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream =  DeployEvidenceSignersData.class.getClassLoader().getResourceAsStream("szt.jks");
        ks.load(ksInputStream, "123456".toCharArray());
        Key key = ks.getKey("ec", "123456".toCharArray());
        ECKeyPair keyPair = ECKeyPair.create(((ECPrivateKey) key).getS());

        Credentials credentials = Credentials.create(keyPair);
        WesignMeshRoute wesignMeshRoute;

        if (routeAddress == null) {
            wesignMeshRoute = WesignMeshRoute.deploy(web3, credentials, BCConstant.gasPrice, BCConstant.gasLimit).sendAsync().get();
        } else {
            wesignMeshRoute = WesignMeshRoute.load(routeAddress, web3, credentials, BCConstant.gasPrice, BCConstant.gasLimit);
        }

        Map<String, JSONObject> nodeMap = new HashMap<>();

        for(Object obj : nodes) {
            JSONObject nodeObj = JSON.parseObject(obj.toString());
            String nodeName = nodeObj.getString("name");
            byte[] nodeNameBytes = new byte[32];
            System.arraycopy(nodeName.getBytes(), 0, nodeNameBytes, 0, nodeName.getBytes().length);

            String nodeIp = nodeObj.getString("ip");
            byte[] nodeIpBytes = new byte[32];
            System.arraycopy(nodeIp.getBytes(), 0, nodeIpBytes, 0, nodeIp.getBytes().length);

            int nodePort = nodeObj.getIntValue("channelPort");

            String nodeOrg = nodeObj.getString("org");
            byte[] nodeOrgBytes = new byte[32];
            System.arraycopy(nodeOrg.getBytes(), 0, nodeOrgBytes, 0, nodeOrg.getBytes().length);

            TransactionReceipt transactionReceipt = wesignMeshRoute.addNode(nodeNameBytes, nodeIpBytes, BigInteger.valueOf(nodePort), nodeOrgBytes).send();
            List<WesignMeshRoute.AddNodeLogEventResponse> nodeLog = wesignMeshRoute.getAddNodeLogEvents(transactionReceipt);
            if (nodeLog != null && nodeLog.size() == 1) {
                WesignMeshRoute.AddNodeLogEventResponse response = nodeLog.get(0);
                if (response.id.intValue() > 0) {
                    //means ok
                    nodeMap.put(nodeName, nodeObj);
                    System.out.println("\n" + nodeName + " add success.");
                } else {
                    System.out.println("\n" + nodeName + " add failed.");
                    System.exit(0);
                }
            }
        }


        for(Object obj : orgs) {
            JSONObject orgObj = JSON.parseObject(obj.toString());
            String orgName = orgObj.getString("name");
            byte[] orgNameBytes = new byte[32];
            System.arraycopy(orgName.getBytes(), 0, orgNameBytes, 0, orgName.getBytes().length);

            TransactionReceipt transactionReceipt = wesignMeshRoute.addOrg(orgNameBytes).send();
            List<WesignMeshRoute.AddOrgLogEventResponse> orgLog = wesignMeshRoute.getAddOrgLogEvents(transactionReceipt);
            if (orgLog != null && orgLog.size() == 1) {
                WesignMeshRoute.AddOrgLogEventResponse response = orgLog.get(0);
                if (response.id.intValue() > 0) {
                    //means ok
                	System.out.println("\n" + orgName + " add success.");
                } else {
                    System.out.println("\n" + orgName + " add failed.");
                    System.exit(0);
                }
            }
        }

        for(Object obj : sets) {
            JSONObject setObj = JSON.parseObject(obj.toString());
            String setName = setObj.getString("name");
            byte[] setNameBytes = new byte[32];
            System.arraycopy(setName.getBytes(), 0, setNameBytes, 0, setName.getBytes().length);

            String evidenceFactoryAddress = null;

            JSONArray nodeNames = setObj.getJSONArray("nodes");
            if (nodeNames.size() > 0)  {
                String firstNodeName = nodeNames.getString(0);
                if (nodeMap.get(firstNodeName) == null) {
                    System.out.println("nodesMap not found key:" + firstNodeName);
                    System.exit(0);
                }

                String ip = nodeMap.get(firstNodeName).getString("ip");
                int nodePort = nodeMap.get(firstNodeName).getIntValue("channelPort");
//                ConcurrentHashMap<String, ChannelConnections> allChannelConnections = new ConcurrentHashMap<>();
//                GroupChannelConnectionsConfig
//                ChannelConnections connections = new ChannelConnections();
//                List<String> connectionStrList = new ArrayList<>();
//                connectionStrList.add(String.format("node@%s:%d", ip, nodePort));
//                connections.setConnectionsStr(connectionStrList);
//                allChannelConnections.put(setName, connections);


                Service setService = new Service();
                setService.setOrgID(setName);
                ChannelConnections channelConnections = new ChannelConnections();
                List<String> ilist = new ArrayList<>();
                ilist.add(ip+":"+nodePort);
                channelConnections.setConnectionsStr(ilist);
                channelConnections.setGroupId(groupId);
                List<ChannelConnections> channelConnectionsList = new ArrayList<>();
                channelConnectionsList.add(channelConnections);
                GroupChannelConnectionsConfig groupChannelConnectionsConfig= new GroupChannelConnectionsConfig();
               groupChannelConnectionsConfig.setAllChannelConnections(channelConnectionsList);
                setService.setAllChannelConnections(groupChannelConnectionsConfig);
                setService.setThreadPool(service.getThreadPool());
                setService.setGroupId(groupId);
                setService.run();
                Thread.sleep(3000);

                /*PublicAddressConf conf = context.getBean(PublicAddressConf.class);
                ConcurrentHashMap<String, String> addressConf = conf.getAllPublicAddress();*/
                JSONArray publicAddressArray = setObj.getJSONArray("publicAddress");
                ConcurrentHashMap<String, String> addressConf = new ConcurrentHashMap<String, String>();
                for (Object publicAddress : publicAddressArray) {
                	if (publicAddress != null) {
                		addressConf.put(publicAddress.toString().split("\\.")[0], publicAddress.toString().split("\\.")[1]);
                	}
                }
                System.out.printf("\nget publicAddress success: %s", addressConf.toString());
                List<String> evidenceSigners = new ArrayList<String>(addressConf.values());
                ChannelEthereumService setChannelEthereumService = new ChannelEthereumService();
                setChannelEthereumService.setChannelService(setService);

                EvidenceSignersData evidenceSignersData = EvidenceSignersData.deploy(Web3j.build(setChannelEthereumService, groupId), credentials, BCConstant.gasPrice, BCConstant.gasLimit,evidenceSigners).sendAsync().get();
                evidenceFactoryAddress = evidenceSignersData.getContractAddress();
            } else {
                System.out.println("nodes's size is 0.");
                System.exit(0);
            }


            List<byte[]> bytes32List = new ArrayList<>();

            for(Object nodeNameObj : nodeNames) {
                String nodeName = nodeNameObj.toString();
                byte[] nodeNameBytes = new byte[32];
                System.arraycopy(nodeName.getBytes(), 0, nodeNameBytes, 0, nodeName.getBytes().length);
                bytes32List.add(nodeNameBytes);
            }

            TransactionReceipt transactionReceipt = wesignMeshRoute.addSet(setNameBytes, bytes32List, evidenceFactoryAddress).send();
            List<WesignMeshRoute.AddSetLogEventResponse> setLog = wesignMeshRoute.getAddSetLogEvents(transactionReceipt);
            if (setLog != null && setLog.size() == 1) {
                WesignMeshRoute.AddSetLogEventResponse response = setLog.get(0);
                if (response.id.intValue() > 0) {
                    //means ok
                	System.out.println("\n" + setName + " add success.");
                } else {
                    System.out.println("\n" + setName + " add failed.");
                    System.exit(0);
                }
            }
        }

        Map<String, Boolean> appidMap = new HashMap<>();

        for(Object obj : rules) {
            JSONObject ruleObj = JSON.parseObject(obj.toString());
            String ruleName = ruleObj.getString("name");
            byte[] ruleNameBytes = new byte[32];
            System.arraycopy(ruleName.getBytes(), 0, ruleNameBytes, 0, ruleName.getBytes().length);

            String appid = ruleObj.getString("appid");
            appidMap.put(appid, true);
            byte[] appidBytes = new byte[32];
            System.arraycopy(appid.getBytes(), 0, appidBytes, 0, appid.getBytes().length);

            int seqMin = ruleObj.getIntValue("seqMin");
            int seqMax = ruleObj.getIntValue("seqMax");

            JSONArray orgNames = ruleObj.getJSONArray("orgs");
            List<byte[]> orgBytes32List = new ArrayList<>();

            for(Object orgNameObj : orgNames) {
                String orgName = orgNameObj.toString();
                logger.debug("rules's orgName: {}", orgName);
                byte[] orgNameBytes = new byte[32];
                System.arraycopy(orgName.getBytes(), 0, orgNameBytes, 0, orgName.getBytes().length);
                orgBytes32List.add(orgNameBytes);
            }
            JSONArray setNames = ruleObj.getJSONArray("sets");
            List<byte[]> bytes32List = new ArrayList<>();

            for(Object setNameObj : setNames) {
                String setName = setNameObj.toString();
                byte[] setNameBytes = new byte[32];
                System.arraycopy(setName.getBytes(), 0, setNameBytes, 0, setName.getBytes().length);
                bytes32List.add(setNameBytes);
            }

            TransactionReceipt transactionReceipt = wesignMeshRoute.addRule(appidBytes, ruleNameBytes,
                    BigInteger.valueOf(seqMin), BigInteger.valueOf(seqMax), orgBytes32List, bytes32List).send();
            List<WesignMeshRoute.AddRuleLogEventResponse> orgLog = wesignMeshRoute.getAddRuleLogEvents(transactionReceipt);
            if (orgLog != null && orgLog.size() == 1) {
                WesignMeshRoute.AddRuleLogEventResponse response = orgLog.get(0);
                if (response.code.intValue() == 0) {
                	System.out.println("\n" + ruleName + " add success.");
                } else if (response.code.intValue() == -1) {
                	System.out.println("\n" + ruleName + " already exists.Please check it!");
                	System.exit(0);
                } else if (response.code.intValue() == -2) {
                	System.out.println("\nThe org contained in the " + ruleName + " does not exists.Please check it!");
                	System.exit(0);
                } else if (response.code.intValue() == -3) {
                	System.out.println("\nThe set contained in the " + ruleName + " does not exists.Please check it!");
                	System.exit(0);
                } else {
                    System.out.println("\n" + ruleName + " add failed.");
                    System.exit(0);
                }
            }
        }

        if (routeAddress == null) {
            System.out.println("\n register route ok!route address:" + wesignMeshRoute.getContractAddress());
        }

        if (appidMap.size() > 0) {
        	for (String appid : appidMap.keySet()) {
        		getAllRules(service, routeAddress == null ? wesignMeshRoute.getContractAddress() : routeAddress, appid);
        	}
        }

        System.exit(0);
    }

    public static void updateNode(Service service, String routeAddress, String nodeName, String ip, int channelPort, String org) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, ExecutionException, InterruptedException, Exception {
    	logger.debug("start handle updateNode...");
    	
    	byte[] nodeNameBytes = new byte[32];
        System.arraycopy(nodeName.getBytes(), 0, nodeNameBytes, 0, nodeName.getBytes().length);

        byte[] ipBytes = new byte[32];
        System.arraycopy(ip.getBytes(), 0, ipBytes, 0, ip.getBytes().length);

        byte[] orgBytes = new byte[32];
        System.arraycopy(org.getBytes(), 0, orgBytes, 0, org.getBytes().length);

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        Web3j web3 = Web3j.build(channelEthereumService, service.getGroupId());

        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream =  DeployEvidenceSignersData.class.getClassLoader().getResourceAsStream("szt.jks");
        ks.load(ksInputStream, "123456".toCharArray());
        Key key = ks.getKey("ec", "123456".toCharArray());
        ECKeyPair keyPair = ECKeyPair.create(((ECPrivateKey) key).getS());

        Credentials credentials = Credentials.create(keyPair);
        WesignMeshRoute wesignMeshRoute = WesignMeshRoute.load(routeAddress, web3, credentials, BCConstant.gasPrice, BCConstant.gasLimit);

        TransactionReceipt transactionReceipt = wesignMeshRoute.updateNode(nodeNameBytes, ipBytes,
                BigInteger.valueOf(channelPort), orgBytes).send();
        List<WesignMeshRoute.UpdateNodeLogEventResponse> logList = wesignMeshRoute.getUpdateNodeLogEvents(transactionReceipt);
        if (logList.size() > 0) {
            WesignMeshRoute.UpdateNodeLogEventResponse response = logList.get(0);
            if (response.code.intValue() == 0) {
                System.out.println("updateNode success.");
            } else {
                System.out.println("updateNode not found node name.");
            }
        } else {
            System.out.println("updateNode not found event log");
        }

        System.exit(0);
    }

    public static void removeSetNode(Service service, String routeAddress, String setName, String nodeName) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, ExecutionException, InterruptedException, Exception {
    	logger.debug("start handle removeSetNode...");
    	
    	byte[] nodeNameBytes = new byte[32];
        System.arraycopy(nodeName.getBytes(), 0, nodeNameBytes, 0, nodeName.getBytes().length);

        byte[] setNameBytes = new byte[32];
        System.arraycopy(setName.getBytes(), 0, setNameBytes, 0, setName.getBytes().length);

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        Web3j web3 = Web3j.build(channelEthereumService, service.getGroupId());

        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream =  DeployEvidenceSignersData.class.getClassLoader().getResourceAsStream("szt.jks");
        ks.load(ksInputStream, "123456".toCharArray());
        Key key = ks.getKey("ec", "123456".toCharArray());
        ECKeyPair keyPair = ECKeyPair.create(((ECPrivateKey) key).getS());

        Credentials credentials = Credentials.create(keyPair);
        WesignMeshRoute wesignMeshRoute = WesignMeshRoute.load(routeAddress, web3, credentials, BCConstant.gasPrice, BCConstant.gasLimit);

        TransactionReceipt transactionReceipt = wesignMeshRoute.removeSetNode(setNameBytes,nodeNameBytes).sendAsync().get();

        List<WesignMeshRoute.RemoveNodeLogEventResponse> logList = wesignMeshRoute.getRemoveNodeLogEvents(transactionReceipt);
        if (logList.size() > 0) {
            WesignMeshRoute.RemoveNodeLogEventResponse response = logList.get(0);
            if (response.code.intValue() == 0) {
                System.out.println("removeSetNode success.");
            } else if (response.code.intValue() == -1){
                System.out.println("removeSetNode not found node name.");
            } else if (response.code.intValue() == -2) {
                System.out.println("removeSetNode not found set name.");
            } else {
                System.out.println("removeSetNode not found node name in set.");
            }
        } else {
            System.out.println("removeSetNode not found event log");
        }

        System.exit(0);

    }

    public static void addSetNode(Service service, String routeAddress, String setName, String nodeName, String ip, int channelPort, String org) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, ExecutionException, InterruptedException, Exception {
    	logger.debug("start handle addSetNode...");
    	
    	byte[] nodeNameBytes = new byte[32];
        System.arraycopy(nodeName.getBytes(), 0, nodeNameBytes, 0, nodeName.getBytes().length);

        byte[] ipBytes = new byte[32];
        System.arraycopy(ip.getBytes(), 0, ipBytes, 0, ip.getBytes().length);

        byte[] orgBytes = new byte[32];
        System.arraycopy(org.getBytes(), 0, orgBytes, 0, org.getBytes().length);

        byte[] setNameBytes = new byte[32];
        System.arraycopy(setName.getBytes(), 0, setNameBytes, 0, setName.getBytes().length);

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        Web3j web3 = Web3j.build(channelEthereumService, service.getGroupId());

        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream =  DeployEvidenceSignersData.class.getClassLoader().getResourceAsStream("szt.jks");
        ks.load(ksInputStream, "123456".toCharArray());
        Key key = ks.getKey("ec", "123456".toCharArray());
        ECKeyPair keyPair = ECKeyPair.create(((ECPrivateKey) key).getS());

        Credentials credentials = Credentials.create(keyPair);
        WesignMeshRoute wesignMeshRoute = WesignMeshRoute.load(routeAddress, web3, credentials, BCConstant.gasPrice, BCConstant.gasLimit);

        TransactionReceipt transactionReceipt = wesignMeshRoute.addSetNode(setNameBytes, nodeNameBytes, ipBytes,
                BigInteger.valueOf(channelPort), orgBytes).send();
        List<WesignMeshRoute.AddSetNodeLogEventResponse> logList = wesignMeshRoute.getAddSetNodeLogEvents(transactionReceipt);
        if (logList.size() > 0) {
            WesignMeshRoute.AddSetNodeLogEventResponse response = logList.get(0);
            if (response.code.intValue() == 0) {
                System.out.println("addSetNode success.");
            } else {
                System.out.println("addSetNode error.code:" + response.code.intValue());
            }
        } else {
            System.out.println("addSetNode not found event log");
        }

        System.exit(0);
    }

    public static void getNodeInfo(Service service, String routeAddress, String nodeName) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, ExecutionException, InterruptedException, Exception {
    	logger.debug("start handle getNodeInfo...");
    	
    	byte[] nodeNameBytes = new byte[32];
        System.arraycopy(nodeName.getBytes(), 0, nodeNameBytes, 0, nodeName.getBytes().length);

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        Web3j web3 = Web3j.build(channelEthereumService, service.getGroupId());

        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream =  DeployEvidenceSignersData.class.getClassLoader().getResourceAsStream("szt.jks");
        ks.load(ksInputStream, "123456".toCharArray());
        Key key = ks.getKey("ec", "123456".toCharArray());
        ECKeyPair keyPair = ECKeyPair.create(((ECPrivateKey) key).getS());

        Credentials credentials = Credentials.create(keyPair);
        WesignMeshRoute wesignMeshRoute = WesignMeshRoute.load(routeAddress, web3, credentials, BCConstant.gasPrice, BCConstant.gasLimit);

        Tuple3<byte[], BigInteger, byte[]> typeList = wesignMeshRoute.getNode(nodeNameBytes).send();
        if(typeList == null){
            System.out.println("not found node info.");
        }
        byte[] ip = typeList.getValue1();
        BigInteger port = typeList.getValue2();
        byte[] org = typeList.getValue3();
        System.out.printf("ip:%s, channelPort:%d, org:%s\n", ip.toString(), port.intValue(), org.toString());
        System.exit(0);
    }

    public static void getSetNodes(Service service, String routeAddress, String setName) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, ExecutionException, InterruptedException, Exception {
    	logger.debug("start handle getSetNodes...");
    	
    	byte[] setNameBytes = new byte[32];
        System.arraycopy(setName.getBytes(), 0, setNameBytes, 0, setName.getBytes().length);

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        Web3j web3 = Web3j.build(channelEthereumService, service.getGroupId());

        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream =  DeployEvidenceSignersData.class.getClassLoader().getResourceAsStream("szt.jks");
        ks.load(ksInputStream, "123456".toCharArray());
        Key key = ks.getKey("ec", "123456".toCharArray());
        ECKeyPair keyPair = ECKeyPair.create(((ECPrivateKey) key).getS());

        Credentials credentials = Credentials.create(keyPair);
        WesignMeshRoute wesignMeshRoute = WesignMeshRoute.load(routeAddress, web3, credentials, BCConstant.gasPrice, BCConstant.gasLimit);

        List nodes = wesignMeshRoute.getSetNodes(setNameBytes).send();
        String str = "";
        for (int i=0; i<nodes.size(); i++) {
            str += nodes.get(i).toString() + ",";
        }

        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }

        System.out.println("set nodes:" + str);
        System.exit(0);
    }

    public static Service initRouteService() throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service service = context.getBean(Service.class);
//        service.setOrgID("WB");
        service.run();
        Thread.sleep(3000);
        return service;
    }

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("miss args:1)deploy/insert/select/delete/updateNode/removeSetNode/addSetNode/getNodeInfo/getSetNodes");
            return;
        }

        String action = args[0];
        String[] newArgs = new String[args.length - 1];

        for (int i = 1; i < args.length; i++) {
            newArgs[i-1] = args[i];
        }

        switch (action) {
            case "deploy":
                deployOrInserRoute(initRouteService(), newArgs);
                break;
            case "insert":
                deployOrInserRoute(initRouteService(), newArgs);
                break;
            case "delete":
                if (newArgs.length != 3) {
                    System.out.println("err args:1)route address 2)appid 3)rule name");
                    return;
                }

                deleteRules(initRouteService(), newArgs[0], newArgs[1], newArgs[2]);
                break;
            case "select":
                if (newArgs.length != 2) {
                    System.out.println("err args:1)route address 2)appid");
                    return;
                }

                getAllRules(initRouteService(), newArgs[0], newArgs[1]);
                break;
            case "updateNode":
                if (newArgs.length != 5) {
                    System.out.println("err args:1)route address 2)node name 3)ip 4)channel port 5)org");
                    return;
                }

                updateNode(initRouteService(), newArgs[0], newArgs[1], newArgs[2], Integer.parseInt(newArgs[3]), newArgs[4]);
                break;
            case "removeSetNode":
                if (newArgs.length != 3) {
                    System.out.println("err args:1)route address 2)set name 3)node name");
                    return;
                }

                removeSetNode(initRouteService(), newArgs[0], newArgs[1], newArgs[2]);
                break;
            case "addSetNode":
                if (newArgs.length != 6) {
                    System.out.println("err args:1)route address 2)set name 3)node name 4)ip 5)channel port 6)org");
                    return;
                }

                addSetNode(initRouteService(), newArgs[0], newArgs[1], newArgs[2], newArgs[3], Integer.parseInt(newArgs[4]), newArgs[5]);
                break;
            case "getNodeInfo":
                if (newArgs.length != 2) {
                    System.out.println("err args:1)route address 2)node name");
                    return;
                }

                getNodeInfo(initRouteService(), newArgs[0], newArgs[1]);
                break;
            case "getSetNodes":
                if (newArgs.length != 2) {
                    System.out.println("err args:1)route address 2)set name");
                    return;
                }

                getSetNodes(initRouteService(), newArgs[0], newArgs[1]);
                break;
            default:
                System.out.println("error command");
        }

        System.exit(0);
    }
}
