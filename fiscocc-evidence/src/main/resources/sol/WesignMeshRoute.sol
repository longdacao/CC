pragma solidity ^0.4.4;

contract WesignMeshRoute {
    struct Rule {
        bytes32 appid;
        bytes32 name;//规则名称
        uint256 seqMin;//id下限
        uint256 seqMax;//id上限
        bytes32[] orgs;//参与的机构
        bytes32[] sets;//set集合
    }

    struct Set {
        uint id;
        bytes32 name;
        bytes32[] nodes;
        address evidenceFactoryAddress;
    }

    struct Node {
        uint id;
        bytes32 name;
        bytes32 ip;
        uint channelPort;
        bytes32 org;
    }

    struct Org {
        uint id;
        bytes32 name;
    }


    mapping(bytes32 => Rule[]) rules;
    mapping(bytes32 => Set) sets;
    mapping(bytes32 => Node) nodes;
    mapping(bytes32 => Org) orgs;

    uint incrSet;
    uint incrNode;
    uint incrOrg;

    event addOrgLog(uint id);
    event addRuleLog(int code);
    event getSetLog(int code, int id, bytes32 name);
    event addSetLog(uint id);
    event addNodeLog(uint id);
    event updateNodeLog(int code);
    event removeNodeLog(int code);
    event addSetNodeLog(int code);
    event delRuleLog(int code, string msg);

    event getRuleLog(uint id, bytes32 appid);

    function addRule(bytes32 appid, bytes32 name, uint256 min, uint256 max, bytes32[] orgNames, bytes32[] setNames) public returns(bool) {
    	for (uint i = 0; i < rules[appid].length; i++) {
	    	if (name == rules[appid][i].name) {
	    		addRuleLog(-1);
	    		return false;
	    	}
	    }
    	
    	for(i = 0; i < orgNames.length; i++) {
			if (orgs[orgNames[i]].id <= 0) {
				addRuleLog(-2);
    			return false;
			}
		}
    	
    	for(i = 0; i < setNames.length; i++) {
			if (sets[setNames[i]].id <= 0) {
				addRuleLog(-3);
    			return false;
			}
		}
    
        Rule memory rule = Rule(appid, name, min, max, orgNames, setNames);
        rules[appid].push(rule);
        addRuleLog(0);
        return true;
    }

    function addSet(bytes32 name, bytes32[] nodeNames, address add) public returns(bool) {
    	if (sets[name].id > 0) {
    		addSetLog(sets[name].id);
    		return true;
    	}

        incrSet = incrSet + 1;
        Set memory set = Set(incrSet, name, nodeNames, add);
        sets[name] = set;
        addSetLog(incrSet);
        return true;
    }

	function getNode(bytes32 name) constant public returns(bytes32, uint, bytes32) {
		var node = nodes[name];
		return (node.ip, node.channelPort, node.org);
	}

	function getSetNodes(bytes32 name) constant public returns(bytes32[]) {
		var set = sets[name];
		return set.nodes;
	}

    function addNode(bytes32 name, bytes32 ip, uint channelPort, bytes32 org) public returns(bool) {

    	if (nodes[name].id > 0) {
    		addNodeLog(nodes[name].id);
    		return true;
    	}

        incrNode = incrNode + 1;
        Node memory node = Node(incrNode, name, ip, channelPort, org);
        addNodeLog(incrNode);
        nodes[name] = node;
    }

	function updateNode(bytes32 name, bytes32 ip, uint channelPort, bytes32 org) public returns(bool) {
		if (nodes[name].id <= 0) {
        	updateNodeLog(-1);
            return false;
        }

        var node = nodes[name];
        node.ip = ip;
        node.channelPort = channelPort;
        node.org = org;
        nodes[name] = node;
        updateNodeLog(0);
        return true;
	}

	function removeSetNode(bytes32 setName, bytes32 nodeName) public returns(bool) {
		if (nodes[nodeName].id <= 0) {
			removeNodeLog(-1);
			return false;
		}

		if (sets[setName].id <= 0) {
			removeNodeLog(-2);
			return false;
		}

		var set = sets[setName];
		var nodeNames = set.nodes;
		var index = nodeNames.length + 1;
		for(uint i = 0; i < nodeNames.length; i++) {
			if (nodeNames[i] == nodeName) {
				index = i;
				break;
			}
		}

		if (index > nodeNames.length) {
			removeNodeLog(-3);
			return false;
		}

		for (i = index; i < nodeNames.length - 1; i++) {
        	nodeNames[i] = nodeNames[i+1];
        }

        --nodeNames.length;
        set.nodes = nodeNames;
        sets[setName] = set;
        removeNodeLog(0);
        return true;
	}

	function addSetNode(bytes32 setName, bytes32 nodeName, bytes32 ip, uint channelPort, bytes32 org) public returns(bool) {

		if (sets[setName].id <= 0) {
			addSetNodeLog(-1);
			return false;
		}

		if (nodes[nodeName].id == 0) {
			incrNode = incrNode + 1;
        	Node memory node = Node(incrNode, nodeName, ip, channelPort, org);
        	nodes[nodeName] = node;
    	}

		var set = sets[setName];
		bool ok = false;
		for (uint i = 0; i < set.nodes.length; i++) {
			if (set.nodes[i] == nodeName) {
				ok = true;
				break;
			}
		}

		if (ok == false) {
			bytes32[] memory tmpNode = new bytes32[](set.nodes.length + 1);
			for(i = 0; i < set.nodes.length; i ++) {
				tmpNode[i] = set.nodes[i];
			}

			tmpNode[set.nodes.length] = nodeName;

			set.nodes = tmpNode;
           	sets[setName] = set;
           	addSetNodeLog(0);
            return true;
		} else {
			addSetNodeLog(-2);
			return false;
		}
	}

    function addOrg(bytes32 name) public returns(bool) {
    	if (orgs[name].id > 0) {
    		return true;
    	}

        incrOrg = incrOrg + 1;
        Org memory org = Org(incrOrg, name);
        orgs[name] = org;
        addOrgLog(incrOrg);
        return true;
    }


    function getRuleNodes(bytes32 appid, uint256 seq) constant public returns(bytes32) {
        var matchRules = rules[appid];
        var length = matchRules.length;

        for (uint i = 0; i < length; i++) {
            var rule = matchRules[i];
            if (seq >= rule.seqMin && seq <= rule.seqMax) {
                var tmpSets = rule.sets;
                if (tmpSets.length != 0) {
                    var mod = seq % tmpSets.length;
                    var setName = tmpSets[mod];
                    return setName;
                }
            }
        }
         
        bytes32 empty;
        return (empty);
    }

    function getEvidenceFactoryAddr(bytes32 setName) constant public returns(address) {
        return sets[setName].evidenceFactoryAddress;
    }

    function getNodes(bytes32 setName, bytes32 org) constant public returns(bytes32[], uint[]) {
        var set = sets[setName];
        var length = set.nodes.length;

        bytes32[] memory ips = new bytes32[](length);
        uint[] memory channelPorts = new uint[](length);
    
        for(uint i = 0; i < length; i++) {
            var nodeName = set.nodes[i];
            var node = nodes[nodeName];
            if (node.org == "" || node.org == org) {
            	ips[i] = node.ip;
                channelPorts[i] = node.channelPort;
            }

        }

        return (ips, channelPorts);
    }

    function getAppidNodes(bytes32 appid, bytes32 org) constant public returns(bytes32[], uint[], bytes32[]) {

        bytes32[] memory resIps = new bytes32[](incrNode * incrSet * rules[appid].length);
        uint[] memory resChannelPorts = new uint[](incrNode * incrSet * rules[appid].length);
        bytes32[] memory resSetNames = new bytes32[](incrNode * incrSet * rules[appid].length);

        uint index = 0;
        for (uint i = 0; i < rules[appid].length; i++) {
            var setNames = rules[appid][i].sets;
            var setLength = setNames.length;
            for (uint j = 0; j < setLength; j++) {
                for (uint x = 0; x < sets[setNames[j]].nodes.length; x++) {
                    resSetNames[index] = setNames[j];
                    if (nodes[sets[setNames[j]].nodes[x]].org == "" || nodes[sets[setNames[j]].nodes[x]].org == org) {
                    	resIps[index] = nodes[sets[setNames[j]].nodes[x]].ip;
                        resChannelPorts[index] = nodes[sets[setNames[j]].nodes[x]].channelPort;
                        index = index + 1;
                    }

                }

            }
        }

        return (resIps, resChannelPorts, resSetNames);
    }

    function getAllRules(bytes32 appid) constant public returns(bytes32[], uint256[], uint256[], bytes32[]) {

    	bytes32[] memory ruleNames = new bytes32[](rules[appid].length);
		uint256[] memory seqMins = new uint[](rules[appid].length);
		uint256[] memory seqMaxs = new uint256[](rules[appid].length);
		////比如，rules有三条，rule0->(set0,set1),rule1->(set0,set2,set3),rule2->(set0,set1,set2),再"|"分隔每个rule
		bytes32[] memory setNames = new bytes32[](rules[appid].length * incrSet + rules[appid].length);
		uint incr = 0;

		for (uint i = 0; i < rules[appid].length; i++) {
			ruleNames[i] = rules[appid][i].name;
			seqMins[i] = rules[appid][i].seqMin;
			seqMaxs[i] = rules[appid][i].seqMax;
			for (uint j = 0; j < rules[appid][i].sets.length; j++) {
				setNames[incr] = rules[appid][i].sets[j];
				incr++;
			}

			setNames[incr] = "|";
			incr++;
		}

		return (ruleNames, seqMins, seqMaxs, setNames);
    }

    function deleteRule(bytes32 appid, bytes32 name) public returns(bool) {
    	if (rules[appid].length == 0) {
    		delRuleLog(-1, "rules size = 0");
    		return false;
    	}

    	var length = rules[appid].length;
    	var ruleList = rules[appid];

    	uint index = length + 1;

    	for(uint i = 0; i < length; i++) {
    		if (ruleList[i].name == name) {
    			index = i;
    			break;
    		}
    	}

    	if (index > length) {
    		delRuleLog(-2, "not found rule name");
    		return false;
    	}



    	for (i = index; i < length - 1; i++) {
    		ruleList[i] = ruleList[i+1];
    	}

    	--ruleList.length;
    	rules[appid] = ruleList;
    	delRuleLog(0, "");

    	return true;
    }
}