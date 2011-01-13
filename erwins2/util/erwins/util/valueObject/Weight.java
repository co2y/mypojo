package erwins.util.valueObject;

import java.math.BigDecimal;





/**
 * 중량을 나타낸다.
 * @author erwins(my.pojo@gmail.com)
 */
public class Weight extends Numerical{

    public Weight(BigDecimal value) {
        super(value);
    }

    @Override
    protected String getUnit() {
        return "Kg";
    }
   
}