/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.plantuml;

/**
 * The type of diagrams supported by the PlantUML macro.
 *
 * @version $Id$
 * @since 2.3
 */
public enum PlantUMLDiagramType
{
    /**
     * A PlantUML diagram.
     */
    plantuml,

    /**
     * A DITAA diagram.
     */
    ditaa,

    /**
     * A GraphViz (dot) diagram.
     */
    dot,

    /**
     * A JCCKIT diagram.
     */
    jcckit,

    /**
     * A Salt diagram.
     */
    salt,

    /**
     * A mindmap diagram.
     * @since 2.3.1
     */
    mindmap,

    /**
     * A regex diagram.
     * @since 2.3.1
     */
    regex,

    /**
     * A gantt diagram.
     * @since 2.3.1
     */
    gantt,

    /**
     * A chronology diagram.
     * @since 2.3.1
     */
    chronology,

    /**
     * A work breakdown structure diagram.
     * @since 2.3.1
     */
    wbs,

    /**
     * An extended backus-naur form.
     * @since 2.3.1
     */
    ebnf,

    /**
     * A JSON diagram.
     * @since 2.3.1
     */
    json,

    /**
     * A YAML diagram.
     * @since 2.3.1
     */
    yaml
}
