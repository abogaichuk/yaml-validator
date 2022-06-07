package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Param;
import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.grammar.KeyWord;
import com.example.yamlvalidator.utils.ValidatorUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public class DefaultValueResolver {

    public Resource fill(Schema schema, Resource resources) {
        collectChildren(schema.getChildren())
                .forEach(parameter -> {
                    resources.deepSearch(parameter.getParent().getPath())
                            .ifPresentOrElse(resource -> {
                                if (isEmpty(resource.getValue())) {
                                    var parent = resource.getParent();
                                    var filled = new Resource(parameter.getParent().getName(), parameter.getValue(), parent,
                                            resource.getPosition(), resource.getYamlType());
                                    parent.deleteChild(resource);
                                    parent.addChild(filled);
                                }
                            }, () -> {
                                Optional.ofNullable(parameter.getParent().getParent().getPath())
                                        .flatMap(resources::deepSearch)
                                        .ifPresentOrElse(grandParentResource -> {
                                            grandParentResource.addChild(
                                                    new Resource(parameter.getParent().getName(), parameter.getValue(),
                                                            grandParentResource, null,
                                                            Param.YamlType.SCALAR));
                                        }, () -> {
                                            resources.addChild(new Resource(parameter.getParent().getName(), parameter.getValue(),
                                                    resources, null,
                                                    Param.YamlType.SCALAR));
                                        });
                            });
                    System.out.println(parameter);
                });
        return resources;
    }

    private Stream<Param> collectChildren(List<Param> parameters) {
        return parameters.stream()
                .flatMap(parameter -> concat(of(parameter), collectChildren(parameter.getChildren())))
                .map(parameter -> parameter.findChild(KeyWord.DEFAULT.name()))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
