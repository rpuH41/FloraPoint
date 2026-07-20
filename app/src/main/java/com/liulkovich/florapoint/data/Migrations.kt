package com.liulkovich.florapoint.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. New columns: reference-only flag + look-alike differences text
        db.execSQL("ALTER TABLE reference_table ADD COLUMN is_reference_only INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE reference_table ADD COLUMN differences_ru TEXT")
        db.execSQL("ALTER TABLE reference_table ADD COLUMN differences_en TEXT")

        // 2. Reciprocal "differences" text for existing species that already had a look-alike
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У белого гриба сеточка на ножке светлая, почти белая, а не тёмная, трубчатый слой белый или слегка желтоватый и не розовеет с возрастом, а мякоть на срезе не меняет цвет и не горчит.', differences_en = 'The porcini''s stem has a pale, almost white net pattern rather than a dark one, the pore layer is white or pale yellow and doesn''t turn pink with age, and the cut flesh keeps its color and doesn''t taste bitter.' WHERE id = 1
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У настоящей лисички вместо пластинок толстые редкие вильчатые складки, спускающиеся по ножке, а цвет ровный жёлто-оранжевый по всему грибу; она никогда не бывает червивой и не растёт на пнях или гнилой древесине.', differences_en = 'The true chanterelle has thick, widely spaced forked ridges instead of gills, running down the stem, with a uniform yellow-orange color throughout; it is never worm-eaten and never grows on stumps or rotting wood.' WHERE id = 4
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У настоящего опёнка на ножке есть плёнчатое кольцо, шляпка покрыта мелкими тёмными чешуйками, а пластинки светло-кремовые, темнеющие до буро-коричневых с возрастом — без зеленоватого или оливкового оттенка.', differences_en = 'The true honey mushroom has a membranous ring on the stem, small dark scales on the cap, and pale cream gills that darken to brownish with age — with no greenish or olive tint.' WHERE id = 5
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У рыжика млечный сок ярко-оранжевый и зеленеет на изломе, а шляпка часто с концентрическими кругами более тёмного оранжевого цвета; сок не белый и не едкий.', differences_en = 'The saffron milk cap''s sap is bright orange and turns green when cut, and the cap often shows concentric rings of a darker orange; the sap is never white or acrid.' WHERE id = 7
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У сыроежки зелёной ножка прямая, ровная, без кольца и без мешковидной вольвы у основания.', differences_en = 'The green russula''s stem is straight and smooth, with no ring and no sack-like volva at the base.' WHERE id = 8
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У груздя настоящего шляпка с бахромчатым мохнатым краем и мягкая на ощупь, а мякоть на изломе заметно темнеет.', differences_en = 'The true milk mushroom''s cap has a fringed, shaggy edge and feels soft, and its flesh visibly darkens when broken.' WHERE id = 9
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У лесного шампиньона пластинки розовые, темнеющие до коричневых с фиолетовым оттенком, а не белые, приятный грибной или миндальный запах, и нет мешковидной вольвы у основания ножки.', differences_en = 'The wood mushroom''s gills are pink, darkening to brown with a purplish tint — never white — it has a pleasant mushroomy or almond smell, and no sack-like volva at the base of the stem.' WHERE id = 11
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У сморчка шляпка коническая, с правильными ячейками, как пчелиные соты, а внутри шляпка и ножка полые.', differences_en = 'The morel''s cap is conical, with regular honeycomb-like cells, and both cap and stem are hollow inside.' WHERE id = 12
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У настоящего дождевика мякоть внутри белая и мягкая, а на верхушке при созревании появляется отверстие для выброса спор.', differences_en = 'A true puffball''s inner flesh is white and soft, and a spore-release opening forms at the top as it matures.' WHERE id = 13
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У польского гриба мякоть на срезе синеет, а трубчатый слой жёлтый и становится сине-зелёным при нажатии; ножка гладкая, буроватая, без сетчатого рисунка.', differences_en = 'The bay bolete''s flesh turns blue when cut, and its yellow pore layer turns blue-green when pressed; the stem is smooth and brownish, with no net pattern.' WHERE id = 14
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У зонтика пёстрого ножка полая с выраженным подвижным белым кольцом, которое свободно двигается по ножке.', differences_en = 'The parasol mushroom has a hollow stem with a prominent movable white ring that slides freely up and down it.' WHERE id = 16
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У рядовки фиолетовой шляпка гладкая, без чешуек и остатков паутинистого покрывала, а пластинки светлее шляпки.', differences_en = 'The wood blewit''s cap is smooth, with no scales or cobweb-veil remnants, and its gills are lighter than the cap.' WHERE id = 19
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У говорушки дымчатой шляпка крупная, мясистая и гладкая, без белого налёта, а запах отчётливый цветочный.', differences_en = 'The clouded agaric''s cap is large, fleshy, and smooth, with no white coating, and it has a distinctive floral smell.' WHERE id = 24
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У бледной поганки на ножке всегда есть плёнчатое кольцо и мешковидная вольва у основания, а пластинки остаются белыми на протяжении всей жизни гриба — сочетание кольца и вольвы отличает её от шампиньонов и сыроежек.', differences_en = 'The death cap always has a membranous ring on the stem and a sack-like volva at the base, and its gills stay white throughout its life — this combination of ring and volva sets it apart from field mushrooms and russulas.' WHERE id = 26
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE reference_table SET differences_ru = 'У строчка шляпка неправильной волнистой формы, похожая на мозг или грецкий орех, тёмно-коричневая или красноватая, а внутри шляпка и ножка заполнены мякотью, а не пустые.', differences_en = 'The false morel''s cap has an irregular, wavy, brain-like or walnut-like shape, dark brown or reddish, and both cap and stem are filled with flesh inside rather than hollow.' WHERE id = 28
            """.trimIndent()
        )

        // 3. New reference-only look-alike species (ids 71-79)
        db.execSQL(
            """
            INSERT INTO reference_table (id, category, name_ru, name_en, habitat_ru, habitat_en, look_alikes_ru, look_alikes_en, description_ru, description_en, start_month, end_month, image_name, is_notif_enabled, is_reference_only, differences_ru, differences_en) VALUES (71, 'mushroom', 'Желчный гриб', 'Bitter bolete', 'Растёт в хвойных и лиственных лесах, часто у оснований деревьев и на трухлявой древесине, встречается с июня по октябрь.', 'Grows in coniferous and deciduous forests, often near tree bases and on decaying wood, from June to October.', 'Белый гриб, Польский гриб', 'Porcini, Bay bolete', 'Несъедобный трубчатый гриб, внешне очень похожий на белый гриб. Мякоть невыносимо горькая, и горечь не пропадает при варке или жарке — испортит любое блюдо, даже если гриб случайно попадёт в корзину в небольшом количестве.', 'An inedible bolete that closely resembles porcini. The flesh is unbearably bitter, and the bitterness doesn''t go away with cooking — even a small amount can ruin an entire dish.', 6, 10, 'tylopilus_felleus', 0, 1, 'На ножке у желчного гриба тёмная сетчатая сеточка вместо светлых чешуек, трубчатый слой розовеет с возрастом и темнеет от нажатия, а мякоть на срезе тоже розовеет — у белого гриба этого не происходит.', 'The stem has a dark net-like pattern instead of light scales, the pore surface turns pink with age and darkens when pressed, and the cut flesh also turns pink — none of this happens in porcini.')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO reference_table (id, category, name_ru, name_en, habitat_ru, habitat_en, look_alikes_ru, look_alikes_en, description_ru, description_en, start_month, end_month, image_name, is_notif_enabled, is_reference_only, differences_ru, differences_en) VALUES (72, 'mushroom', 'Ложная лисичка', 'False chanterelle', 'Растёт на гниющей древесине, пнях и лесной подстилке в хвойных и смешанных лесах, с июля по октябрь.', 'Grows on rotting wood, stumps, and forest litter in coniferous and mixed forests, from July to October.', 'Лисичка', 'Chanterelle', 'Считается слабоядовитым или условно-съедобным грибом низкого качества — у чувствительных людей может вызвать расстройство желудка. Внешне похожа на настоящую лисичку, но мякоть тонкая и рыхлая, а вкус невыразительный.', 'Considered mildly toxic or a low-quality edible — it can cause stomach upset in sensitive people. It resembles the true chanterelle but has thin, brittle flesh and a bland taste.', 7, 10, 'hygrophoropsis_aurantiaca', 0, 1, 'У ложной лисички частые тонкие пластинки, явно сбегающие на ножку, и более яркий однотонный оранжевый цвет; настоящая лисичка имеет толстые редкие складки вместо пластинок и растёт группами, а не поодиночке.', 'The false chanterelle has closely spaced thin gills that clearly run down the stem, and a more uniform bright orange color; the true chanterelle has thick, widely spaced ridges instead of gills and grows in clusters rather than alone.')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO reference_table (id, category, name_ru, name_en, habitat_ru, habitat_en, look_alikes_ru, look_alikes_en, description_ru, description_en, start_month, end_month, image_name, is_notif_enabled, is_reference_only, differences_ru, differences_en) VALUES (73, 'mushroom', 'Ложнодождевик', 'Common earthball', 'Растёт в хвойных и лиственных лесах на почве и трухлявых пнях, с июля по октябрь.', 'Grows in coniferous and deciduous forests on soil and rotting stumps, from July to October.', 'Дождевик', 'Puffball', 'Несъедобный и слабо-ядовитый гриб, вызывающий расстройство желудка при употреблении. Внешне похож на дождевик, но оболочка плотнее и грубее, часто с трещинами и жёлто-бурыми пятнами.', 'An inedible, mildly toxic mushroom that causes stomach upset if eaten. It resembles a puffball but has a thicker, coarser skin, often cracked and marked with yellow-brown patches.', 7, 10, 'scleroderma_citrinum', 0, 1, 'Мякоть у ложнодождевика на срезе тёмная и жёсткая, с запахом сырого картофеля, и в ней никогда не появляется отверстие для выброса спор сверху — у настоящего дождевика мякоть светлая и мягкая, а на верхушке при созревании появляется отверстие.', 'Cut open, the false earthball''s flesh is dark and tough with a raw-potato smell, and it never develops a spore-release opening on top — a true puffball''s flesh is pale and soft, and an opening forms at the top as it matures.')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO reference_table (id, category, name_ru, name_en, habitat_ru, habitat_en, look_alikes_ru, look_alikes_en, description_ru, description_en, start_month, end_month, image_name, is_notif_enabled, is_reference_only, differences_ru, differences_en) VALUES (74, 'mushroom', 'Ложноопёнок серно-жёлтый', 'Sulphur tuft', 'Растёт большими группами на пнях, стволах и корнях лиственных и хвойных деревьев, с апреля по октябрь.', 'Grows in large clusters on stumps, trunks, and roots of deciduous and coniferous trees, from April to October.', 'Опёнок осенний', 'Honey mushroom', 'Ядовитый гриб, который часто селится рядом со съедобными опятами на тех же пнях. Мякоть светло-жёлтая, очень горькая, с неприятным запахом.', 'A poisonous mushroom that often grows right next to edible honey mushrooms on the same stumps. The flesh is pale yellow, very bitter, and has an unpleasant smell.', 4, 10, 'hypholoma_fasciculare', 0, 1, 'У ложноопёнка нет кольца на ножке, шляпка серно-жёлтая с зеленоватым оттенком без чешуек, а пластинки сначала серно-жёлтые, затем зеленовато-оливковые; у настоящего опёнка на ножке есть плёнчатое кольцо, а шляпка покрыта мелкими чешуйками.', 'The false honey mushroom has no ring on the stem, a sulphur-yellow cap with a greenish tint and no scales, and gills that start sulphur-yellow and turn olive-green; the true honey mushroom has a membranous ring on the stem and small scales on the cap.')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO reference_table (id, category, name_ru, name_en, habitat_ru, habitat_en, look_alikes_ru, look_alikes_en, description_ru, description_en, start_month, end_month, image_name, is_notif_enabled, is_reference_only, differences_ru, differences_en) VALUES (75, 'mushroom', 'Мухомор пантерный', 'Panther cap', 'Растёт в широколиственных, сосновых и смешанных лесах на щелочных почвах, с июля по сентябрь.', 'Grows in broadleaf, pine, and mixed forests on alkaline soils, from July to September.', 'Зонтик пёстрый', 'Parasol mushroom', 'Один из самых опасных ядовитых мухоморов, вызывающий тяжёлое отравление вплоть до летального исхода. Шляпка серо-коричневая или оливково-бурая с белыми хлопьями, у основания ножки — воротничковая вольва.', 'One of the most dangerous poisonous Amanita species, capable of causing severe poisoning and even death. The cap is grey-brown or olive-brown with white flecks, and the stem has a collar-like volva at the base.', 7, 9, 'amanita_pantherina', 0, 1, 'У мухомора пантерного массивная ножка без кольца или со слабым, быстро исчезающим кольцом в нижней части, а у основания — характерная воротничковая вольва; у зонтика пёстрого ножка полая, с выраженным подвижным белым кольцом, которое можно сдвинуть по ножке.', 'The panther cap has a solid stem with no ring or only a faint, quickly disappearing one near the base, plus a distinctive collar-like volva at the base; the parasol mushroom has a hollow stem with a prominent movable white ring that slides up and down.')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO reference_table (id, category, name_ru, name_en, habitat_ru, habitat_en, look_alikes_ru, look_alikes_en, description_ru, description_en, start_month, end_month, image_name, is_notif_enabled, is_reference_only, differences_ru, differences_en) VALUES (76, 'mushroom', 'Паутинник фиолетовый', 'Violet webcap', 'Растёт в лиственных и смешанных лесах, образует микоризу с берёзой и осиной, с августа по октябрь. Занесён в Красную книгу.', 'Grows in deciduous and mixed forests, forming mycorrhiza with birch and aspen, from August to October. Protected in several regions.', 'Рядовка фиолетовая', 'Wood blewit', 'Условно-съедобный гриб насыщенного тёмно-фиолетового цвета, встречается редко. Перед употреблением требует отваривания; в некоторых регионах сбор запрещён.', 'A conditionally edible mushroom with a deep violet color, rarely found. It must be boiled before eating, and in some regions collecting it is prohibited.', 8, 10, 'cortinarius_violaceus', 0, 1, 'У паутинника фиолетового пластинки того же тёмно-фиолетового цвета, что и шляпка, а на молодой ножке заметны остатки паутинистого покрывала; у рядовки фиолетовой шляпка гладкая, без чешуек и паутинистых остатков, а пластинки светлее шляпки.', 'The violet webcap has gills the same dark violet color as the cap, and young stems show cobweb-like veil remnants; the wood blewit has a smooth cap with no scales or veil remnants, and gills lighter than the cap.')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO reference_table (id, category, name_ru, name_en, habitat_ru, habitat_en, look_alikes_ru, look_alikes_en, description_ru, description_en, start_month, end_month, image_name, is_notif_enabled, is_reference_only, differences_ru, differences_en) VALUES (77, 'mushroom', 'Скрипица', 'The Miller', 'Растёт в лиственных и смешанных лесах, часто образует микоризу с осиной и дубом, с июля по октябрь.', 'Grows in deciduous and mixed forests, often forming mycorrhiza with aspen and oak, from July to October.', 'Груздь настоящий', 'Milk mushroom', 'Условно-съедобный млечник, уступающий груздю по вкусу. Шляпка плотная, почти пластмассовая на ощупь, издаёт характерный скрип при трении. Ядовитых двойников не имеет.', 'A conditionally edible milkcap, less prized than the true milk mushroom. The cap is dense, almost rubbery to the touch, and makes a characteristic squeak when rubbed. It has no poisonous look-alikes.', 7, 10, 'lactarius_vellereus', 0, 1, 'У скрипицы шляпка более твёрдая и сухая, без бахромчатого края, а мякоть на срезе не темнеет; у груздя настоящего край шляпки бахромчатый, гриб мягче, и мякоть на изломе заметно темнеет.', 'The Miller has a firmer, drier cap with no fringed edge, and the flesh doesn''t darken when cut; the true milk mushroom has a fringed cap edge, is softer overall, and its flesh visibly darkens when broken.')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO reference_table (id, category, name_ru, name_en, habitat_ru, habitat_en, look_alikes_ru, look_alikes_en, description_ru, description_en, start_month, end_month, image_name, is_notif_enabled, is_reference_only, differences_ru, differences_en) VALUES (78, 'mushroom', 'Волнушка', 'Woolly milkcap', 'Растёт в берёзовых рощах и смешанных лесах, образует микоризу с берёзой, с конца июня по конец октября.', 'Grows in birch groves and mixed forests, forming mycorrhiza with birch, from late June to late October.', 'Рыжик', 'Saffron milk cap', 'Условно-съедобный гриб с розоватой шляпкой в концентрических кругах и пушистым краем. Требует вымачивания и отваривания перед засолкой из-за едкого млечного сока.', 'A conditionally edible mushroom with a pinkish cap marked by concentric rings and a fuzzy edge. It requires soaking and boiling before pickling because of its acrid milky sap.', 6, 10, 'lactarius_torminosu', 0, 1, 'У волнушки млечный сок белый и едкий, не меняющий цвет на воздухе; у рыжика млечный сок ярко-оранжевый и на изломе зеленеет — это самый надёжный способ их различить.', 'The woolly milkcap''s sap is white and acrid, and doesn''t change color in the air; the saffron milk cap''s sap is bright orange and turns green when exposed — this is the most reliable way to tell them apart.')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO reference_table (id, category, name_ru, name_en, habitat_ru, habitat_en, look_alikes_ru, look_alikes_en, description_ru, description_en, start_month, end_month, image_name, is_notif_enabled, is_reference_only, differences_ru, differences_en) VALUES (79, 'mushroom', 'Ядовитые говорушки', 'Poisonous clitocybe', 'Растёт на лугах, полянах и опушках, часто «ведьмиными кольцами», с августа по октябрь.', 'Grows in meadows, clearings, and forest edges, often in "fairy rings," from August to October.', 'Говорушка дымчатая', 'Clouded agaric', 'Ядовитая говорушка, содержащая большое количество мускарина. Шляпка беловато-серая, выпукло-распростёртая, с мучнистым запахом и белым мучнистым налётом на поверхности.', 'A poisonous Clitocybe species containing high levels of muscarine. The cap is whitish-grey, convex to flat, with a mealy smell and a white powdery coating.', 8, 10, 'clitocybe_dealbata', 0, 1, 'У ядовитой говорушки на шляпке заметен белый мучнистый налёт, гриб мельче и суше на ощупь; говорушка дымчатая крупнее, мясистее, шляпка гладкая без налёта, а запах — характерный цветочный, а не мучной.', 'The poisonous clitocybe has a white powdery coating on the cap and feels smaller and drier; the clouded agaric is larger and meatier, with a smooth cap and no coating, and a distinctive floral smell rather than a mealy one.')
            """.trimIndent()
        )
    }
}