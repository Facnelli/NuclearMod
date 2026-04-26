package nuclearmod.content;

import arc.graphics.Color;
import mindustry.type.Item;

public class ModItems {
    public static Item iron, steel, uranio, uranioEnriquecido, circuit, plastanium;

    public static void load() {
        iron = new Item("iron", Color.valueOf("74f53d")) {{
            localizedName = "Ferro";
            hardness = 1;
            radioactivity = 0f;
            explosiveness = 0f;
        }};

        steel = new Item("steel", Color.valueOf("7a7a7a")) {{
            localizedName = "Aço";
            description = "Um metal para construção de misseis criado a partir do ferro e grafite.";
            hardness = 0;
            radioactivity = 0f;
            explosiveness = 0f;
        }};

        uranio = new Item("uranio", Color.valueOf("3bd14b")) {{
            localizedName = "Urânio";
            description = "Minério pesado e altamente radioativo. Usado para fabricar ogivas devastadoras.";
            hardness = 4;
            radioactivity = 1.2f;
            explosiveness = 1.0f;
        }};

        uranioEnriquecido = new Item("uranio-enriquecido", Color.valueOf("74f53d")) {{
            localizedName = "Urânio Enriquecido";
            description = "Urânio purificado e altamente instável. Usado exclusivamente para munição de destruição em massa.";
            hardness = 0;
            radioactivity = 10.0f;
            explosiveness = 15.0f;
        }};

        circuit = new Item("circuit", Color.valueOf("094D33")) {{
            localizedName = "Circuito";
            description = "Uma placa de circuito para controle do missil";
        }};
    }
}