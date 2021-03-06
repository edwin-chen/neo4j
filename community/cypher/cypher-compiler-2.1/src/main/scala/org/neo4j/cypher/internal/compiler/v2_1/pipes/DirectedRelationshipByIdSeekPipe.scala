/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
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
package org.neo4j.cypher.internal.compiler.v2_1.pipes

import org.neo4j.cypher.internal.compiler.v2_1.{PlanDescriptionImpl, symbols, ExecutionContext}
import symbols._
import org.neo4j.cypher.internal.compiler.v2_1.commands.expressions.Expression
import org.neo4j.graphdb.Relationship
import org.neo4j.cypher.internal.helpers.CollectionSupport

case class DirectedRelationshipByIdSeekPipe(ident: String, relIdExpr: Expression, toNode: String, fromNode: String)
                                           (implicit pipeMonitor: PipeMonitor) extends Pipe with CollectionSupport {

  protected def internalCreateResults(state: QueryState): Iterator[ExecutionContext] = {
    val relIds = relIdExpr(ExecutionContext.empty)(state)

    if (relIds == null) {
      Iterator(ExecutionContext.from(ident -> null, toNode -> null, fromNode -> null))
    } else {
      val relationshipIds = makeTraversable(relIds).iterator
      new IdSeekIterator[Relationship](ident, state.query.relationshipOps, relationshipIds).map {
        ctx =>
          val r = ctx(ident)
          r match {
            case r: Relationship => ctx += (fromNode -> r.getStartNode) += (toNode -> r.getEndNode)
          }
      }
    }
  }

  def exists(predicate: Pipe => Boolean): Boolean = predicate(this)

  def executionPlanDescription = new PlanDescriptionImpl(this, "DirectedRelationshipByIdSeekPipe", Seq.empty, Seq("ident" -> ident))

  def symbols = new SymbolTable(Map(ident -> CTRelationship, toNode -> CTNode, fromNode -> CTNode))

  def monitor = pipeMonitor
}
