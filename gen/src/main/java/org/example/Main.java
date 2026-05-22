package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.smile_ns.simplejson.SimpleJson;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

//TIP コードを<b>実行</b>するには、<shortcut actionId="Run"/> を押すか
// ガターの <icon src="AllIcons.Actions.Execute"/> アイコンをクリックします。
public class Main {
    static void main() throws IOException {
        SimpleJson json = new SimpleJson(new File("./fragment_block.json"));
        Object[] fragBlocks = json.getList("blocks").toArray();

        for (Object block : fragBlocks) {
            String blockName = block.toString();
            SimpleJson loot = new SimpleJson(new File("./input/blocks/" + blockName + ".json"));
            loot.setFile(new File("./output/blocks/" + blockName + ".json"));
            try {
                Object[] pools = loot.getList("pools").toArray();
                for (int i = 0;i < pools.length;i++) {
                    JsonNode np = new ObjectMapper().valueToTree(pools[i]);
                    SimpleJson pool = new SimpleJson(np);

                    Object[] entries = pool.getList("entries").toArray();
                    pool.put("entries", getEntries(entries));

                    pools[i] = pool.toJsonNode();
                }
                loot.put("pools", pools);
                loot.save();
            } catch (NullPointerException e) {
                System.out.println("failed: " + blockName);
                throw e;
            }
        }
    }

    static JsonNode[] getEntries(Object[] entries) throws IOException {
        JsonNode[] jsonEntries = new JsonNode[entries.length];
        for (int i = 0; i < entries.length; i++) {
            JsonNode n = new ObjectMapper().valueToTree(entries[i]);
            SimpleJson entry = new SimpleJson(n);

            if (entry.getNode("children") == null) {
                String itemName = entry.getString("name").replace("minecraft:", "");
                if (entry.getNode("functions") == null) {
                    entry.put("functions", getInfoJson(itemName));
                } else {
                    List<Object> functions = entry.getList("functions");
                    functions.add(getInfoJson(itemName));
                    entry.put("functions", functions);
                }
            } else {
                Object[] children = entry.getList("children").toArray();
                entry.put("children", getEntries(children));
            }

            jsonEntries[i] = entry.toJsonNode();
        }

        return jsonEntries;
    }

    static List<JsonNode> getInfoJson(String blockName) throws IOException {
        SimpleJson template1 = new SimpleJson("{\"function\": \"minecraft:set_custom_data\",\"tag\":{\"fragment\": true}}");
        SimpleJson template2 = new SimpleJson("{\"function\": \"minecraft:set_name\",\"name\": {\"translate\": \"\",\"italic\": false}}");

        template2.put("name.translate", "item.jumbo_craft." + blockName + "_fragment");

        return Arrays.asList(template1.toJsonNode(), template2.toJsonNode());
    }
}
