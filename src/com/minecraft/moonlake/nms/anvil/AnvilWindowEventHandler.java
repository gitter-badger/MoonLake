/*
 * Copyright (C) 2016 The MoonLake Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 
package com.minecraft.moonlake.nms.anvil;

import com.minecraft.moonlake.execute.Execute;

/**
 * <h1>AnvilWindowEventHandler</h1>
 * 铁砧窗口事件处理接口（详细doc待补充...）
 *
 * @version 1.0
 * @author Month_Light
 */
public interface AnvilWindowEventHandler<T extends AnvilWindowEvent> extends Execute<T> {

    /**
     * 铁砧窗口事件处理接口执行器
     *
     * @param event 事件
     */
    void onExecute(T event);
}
