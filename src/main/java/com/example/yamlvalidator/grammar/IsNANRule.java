//package com.example.yamlvalidator.grammar;
//
//import com.example.yamlvalidator.entity.Param;
//import com.example.yamlvalidator.entity.SchemaParam;
//import com.example.yamlvalidator.entity.ValidationResult;
//import com.example.yamlvalidator.services.MessageProvider;
//import com.example.yamlvalidator.utils.ValidatorUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//import java.util.Arrays;
//import java.util.Optional;
//import java.util.stream.Stream;
//
//import static com.example.yamlvalidator.entity.ValidationResult.invalid;
//import static com.example.yamlvalidator.utils.ValidatorUtils.IS_NAN;
//import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;
//
//@Component
//public class IsNANRule implements SchemaRule {
//    @Autowired
//    private MessageProvider messageProvider;
//
//    @Override
//    public ValidationResult validate(SchemaParam param) {
//        return Stream.of(KeyWord.DEFAULT.name(), KeyWord.MIN.name(), KeyWord.MAX.name())
//                .map(param::findChild)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .filter(Conditions.isNAN)
//                .map(p -> invalid(messageProvider.getMessage(IS_NAN, p.getName(), p.getPath(), p.getRow())))
//                .reduce(ValidationResult.valid(), ValidationResult::merge);
//    }
//}
