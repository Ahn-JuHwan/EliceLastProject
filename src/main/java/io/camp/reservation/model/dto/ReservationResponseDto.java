package io.camp.reservation.model.dto;

import io.camp.campsite.model.Campsite;
import io.camp.reservation.model.ReservationState;
import io.camp.user.model.User;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationResponseDto {
    private Long reservationId;

    private LocalDateTime reserveStartDate;
    private LocalDateTime reserveEndDate;
    private int adults;
    private int children;
    private int totalPrice;
    private LocalDateTime createdAt;

    private ReservationState reservationState;

    @Setter(AccessLevel.NONE)
    private String campsiteName;

    public void setCampsiteName(Campsite campsite){
        this.campsiteName = campsite.getIntroduce();
    }
}
