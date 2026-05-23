# This is a sample Python script.
import json


# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.

lang = json.load(open('./input/ja_jp.json', 'r', encoding='utf-8'))


def main():
    # Use a breakpoint in the code line below to debug your script.
    frag_blocks = json.load(open('./input/fragment_block.json','r',encoding='utf-8'))['blocks']

    for block in frag_blocks:
        loot = json.load(open(f'./input/blocks/{block}.json','r',encoding='utf-8'))

        pools = loot['pools']
        for pool in pools:
            entries = pool['entries']
            pool['entries'] = get_entries(entries)

        path = f'./output/blocks/{block}.json'
        with open(path, mode='w', encoding='utf-8') as f:
            json.dump(loot, f, ensure_ascii=False, indent=4, sort_keys=True, separators=(',', ': '))

        print(f'saved {path}')


def get_entries(entries):
    for entry in entries:
        if 'children' not in entry:
            item_name = entry['name'].replace('minecraft:', '')
            if 'function' in entry:
                functions = entry['functions']
                functions.append(get_info(item_name))
                entry['functions'] = functions
            else:
                entry['functions'] = get_info(item_name)
        else:
            children = entry['children']
            entry['children'] = get_entries(children)

    return entries


def get_info(item_name):
    template1 = {
        'function': 'minecraft:set_custom_data',
        'tag': {
            'fragment': True
        }
    }

    fragment_key = f'block.jumbo_craft.{item_name}_fragment'
    template2 = {
        'function': 'minecraft:set_name',
        'name': {
            'italic': False,
            'translate': fragment_key,
            'fallback': lang[fragment_key]
        } if fragment_key in lang
        else { 'italic': False }
    }

    return template1, template2

# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    main()

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
