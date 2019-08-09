import sys, os, warnings

from ncclient import manager
from ncclient.xml_ import *
import json

def commit(username, password, occurrence):

    dict = json.loads(dynamic_properties)
    dict_ips = json.loads(dict['ips']['host-ip'])

    if (occurrence == "1"):
        ip = dict_ips[0]
        isis = dict['config']['isis']
        isis_xml = "".join( isis.splitlines())
        interface=dict['config']['interface']
        interface_xml = "".join( interface.splitlines())
        push_config(ip, username, password, interface_xml, isis_xml)
    else:
        for i in range(1, int(occurrence) + 1):
            ip = dict_ips[int(i)-1]
            print(ip)
            isis = dict['config'][str(i)]['isis']
            isis_xml = "".join( isis.splitlines())
            interface=dict['config'][str(i)]['interface']
            interface_xml = "".join( interface.splitlines())
            push_config(ip, username, password, interface_xml, isis_xml)

def push_config(ip, username, password, interface_xml, isis_xml):
    print("Start netconf sessions to %s\n" % ip)
    with manager.connect(host=ip, port=830, username=username, password=password, hostkey_verify=False, look_for_keys=False) as m:

        print("Lock\n")
        c = m.lock()
        print("%s\n") % c
        print("Pushing loopback_config edit-config\n")
        c = m.edit_config(config=interface_xml)
        print("%s\n") % c
        print("\n Validate loopback_config")
        c = m.validate()
        print("%s\n") % c
        print("Pushing isis_config edit-config\n")
        c = m.edit_config(config=isis_xml)
        print("\n Validate isis_config")
        c = m.validate()
        print("%s\n") % c
        print("\n Discard changes")
        c = m.discard()
        print("%s\n") % c
        # print("\n Commit")
        # c = m.commit()
        # print(c)
        print("Unlock\n")
        c = m.unlock()
        print("%s\n") % c
        print("\n Close session")
        c = m.close_session()
        print(c)

if __name__ == '__main__':
    commit(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])
