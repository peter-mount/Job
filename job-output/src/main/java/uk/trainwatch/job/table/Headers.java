/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.job.table;

import uk.trainwatch.job.table.visitors.TableVisitor;

/**
 *
 * @author peter
 */
public class Headers
        extends Molecule<Header>
{

    public void add( Header h )
    {
        getElements().add( h );
    }

    @Override
    public void accept( TableVisitor t )
    {
    }

}
