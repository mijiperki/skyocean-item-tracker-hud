package dev.tperkins.multicraft.data

import dev.tperkins.multicraft.craft.ProgressCalculator
import dev.tperkins.multicraft.inventory.OwnedProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecipeResolverTest {

    // ENCHANTED_IRON = 5 * 32 = 160 IRON_INGOT (count 1)
    // ENCHANTED_IRON_BLOCK = 9 * ENCHANTED_IRON  => 1440 IRON_INGOT
    private val repo = ItemRepository.fromJson(
        """
        {
          "IRON_INGOT": { "internalname": "IRON_INGOT", "displayname": "Iron Ingot" },
          "ENCHANTED_IRON": {
            "internalname": "ENCHANTED_IRON", "displayname": "Enchanted Iron",
            "recipes": [{ "type": "crafting",
              "A2": "IRON_INGOT:32", "B1": "IRON_INGOT:32", "B2": "IRON_INGOT:32",
              "B3": "IRON_INGOT:32", "C2": "IRON_INGOT:32", "count": 1 }]
          },
          "ENCHANTED_IRON_BLOCK": {
            "internalname": "ENCHANTED_IRON_BLOCK", "displayname": "Enchanted Iron Block",
            "recipes": [{ "type": "crafting",
              "A1": "ENCHANTED_IRON:1", "A2": "ENCHANTED_IRON:1", "A3": "ENCHANTED_IRON:1",
              "B1": "ENCHANTED_IRON:1", "B2": "ENCHANTED_IRON:1", "B3": "ENCHANTED_IRON:1",
              "C1": "ENCHANTED_IRON:1", "C2": "ENCHANTED_IRON:1", "C3": "ENCHANTED_IRON:1",
              "count": 1 }]
          }
        }
        """.trimIndent(),
    )
    private val resolver = RecipeResolver(repo)

    @Test
    fun `single tier expands to raw material total`() {
        val tree = resolver.resolve("ENCHANTED_IRON", 1)
        assertEquals(1, tree.children.size)
        assertEquals("IRON_INGOT", tree.children[0].internalName)
        assertEquals(160, tree.children[0].requiredQty)
        assertTrue(tree.children[0].isLeaf)
    }

    @Test
    fun `nested recipe sums leaves and respects output count`() {
        val tree = resolver.resolve("ENCHANTED_IRON_BLOCK", 1)
        // The nine ENCHANTED_IRON grid slots collapse into one child requiring 9.
        assertEquals(1, tree.children.size)
        assertEquals("ENCHANTED_IRON", tree.children[0].internalName)
        assertEquals(9, tree.children[0].requiredQty)
        assertEquals(mapOf("IRON_INGOT" to 1440), tree.leafTotals())
    }

    @Test
    fun `amount scales crafts with ceiling division`() {
        // Ask for 2 enchanted iron -> 2 crafts -> 320 iron.
        val tree = resolver.resolve("ENCHANTED_IRON", 2)
        assertEquals(2, tree.craftsNeeded)
        assertEquals(320, tree.children[0].requiredQty)
    }

    @Test
    fun `unknown item resolves to a leaf`() {
        val tree = resolver.resolve("MITHRIL_ORE", 5)
        assertTrue(tree.isLeaf)
        assertEquals(5, tree.requiredQty)
    }

    @Test
    fun `progress reflects owned raw materials independently`() {
        val tree = resolver.resolve("ENCHANTED_IRON_BLOCK", 1)
        val owned = OwnedProvider { name -> if (name == "IRON_INGOT") 720 else 0 }
        val progress = ProgressCalculator.evaluate(tree, owned)
        assertFalse(progress.satisfied)
        assertEquals(0.5, progress.percent, 1e-9)
    }

    @Test
    fun `owning the top item marks it satisfied`() {
        val tree = resolver.resolve("ENCHANTED_IRON", 3)
        val owned = OwnedProvider { name -> if (name == "ENCHANTED_IRON") 3 else 0 }
        val progress = ProgressCalculator.evaluate(tree, owned)
        assertTrue(progress.satisfied)
        assertEquals(1.0, progress.percent, 1e-9)
    }
}
