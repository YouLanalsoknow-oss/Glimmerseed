package com.example.glimmerseed.floatingpreviewbase.test

import com.example.glimmerseed.editorcore.asset.AssetRef
import com.example.glimmerseed.editorcore.asset.AssetSource
import com.example.glimmerseed.editorcore.asset.AssetType
import com.example.glimmerseed.editorcore.coord.NormalizedRect
import com.example.glimmerseed.editorcore.panel.*
import com.example.glimmerseed.editorcore.stage.PanelSlot

object StageTestData {

    fun createDefaultPanelSlot(panelId: String, zOrder: Int = 0): PanelSlot {
        return PanelSlot(
            panelId = panelId,
            zOrder = zOrder,
            active = true,
            landscapeRect = NormalizedRect(0.25f, 0.25f, 0.5f, 0.5f),
            portraitRect = NormalizedRect(0.25f, 0.25f, 0.5f, 0.5f),
            touchMode = TouchMode.PASSTHROUGH
        )
    }

    fun createDemoPanel1(): PanelData {
        return PanelData(
            id = "demo_panel_1",
            name = "演示面板1",
            visual = VisualLayerData(
                type = VisualType.LAYER_RENDERING,
                opacity = 0.8f,
                visible = true
            ),
            interaction = InteractionLayerData(
                touchMode = TouchMode.PASSTHROUGH
            ),
            behavior = BehaviorLayerData()
        )
    }

    fun createDemoPanel2(): PanelData {
        return PanelData(
            id = "demo_panel_2",
            name = "演示面板2",
            visual = VisualLayerData(
                type = VisualType.LAYER_RENDERING,
                opacity = 0.6f,
                visible = true
            ),
            interaction = InteractionLayerData(
                touchMode = TouchMode.BLOCKING
            ),
            behavior = BehaviorLayerData()
        )
    }

    fun createTestPanels(): List<Pair<PanelData, PanelSlot>> {
        val panel1 = createDemoPanel1()
        val slot1 = PanelSlot(
            panelId = panel1.id,
            zOrder = 0,
            active = true,
            landscapeRect = NormalizedRect(0.1f, 0.1f, 0.3f, 0.3f),
            portraitRect = NormalizedRect(0.1f, 0.1f, 0.3f, 0.3f),
            touchMode = TouchMode.PASSTHROUGH
        )

        val panel2 = createDemoPanel2()
        val slot2 = PanelSlot(
            panelId = panel2.id,
            zOrder = 1,
            active = true,
            landscapeRect = NormalizedRect(0.6f, 0.1f, 0.3f, 0.3f),
            portraitRect = NormalizedRect(0.6f, 0.1f, 0.3f, 0.3f),
            touchMode = TouchMode.PASSTHROUGH
        )

        return listOf(panel1 to slot1, panel2 to slot2)
    }
}
